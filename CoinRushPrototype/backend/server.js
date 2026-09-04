const express = require("express");
const cors = require("cors");
const helmet = require("helmet");
const rateLimit = require("express-rate-limit");
const crypto = require("crypto");
const pool = require("./db");
require("dotenv").config();

const app = express();
app.set("trust proxy", 1);
app.use(helmet());
app.use(cors());
app.use(express.json({ limit: "20kb" }));

const limiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 150,
  standardHeaders: true,
  legacyHeaders: false
});
app.use("/api/", limiter);

const sha256 = value => crypto.createHash("sha256").update(value, "utf8").digest("hex");
const makeToken = () => crypto.randomBytes(32).toString("hex");

function validUsername(value) {
  return typeof value === "string" && /^[a-zA-Z0-9_]{3,30}$/.test(value.trim());
}
function validPassword(value) {
  return typeof value === "string" && value.length >= 4 && value.length <= 100;
}
function validDevice(value) {
  return typeof value === "string" && value.trim().length >= 6 && value.trim().length <= 200;
}

function dailyClaimedToday(row) {
  if (!row || !row.last_daily_bonus) return false;
  const d = new Date(row.last_daily_bonus);
  const now = new Date();
  return d.getUTCFullYear() === now.getUTCFullYear() &&
         d.getUTCMonth() === now.getUTCMonth() &&
         d.getUTCDate() === now.getUTCDate();
}

function responseUser(row) {
  return {
    id: row.id, username: row.username, device_id: row.device_id,
    balance_coins: row.balance_coins, best_score: row.best_score,
    last_daily_bonus: row.last_daily_bonus || null
  };
}

async function ensureTables() {
  await pool.query(`
    CREATE TABLE IF NOT EXISTS public.player_balances (
      id BIGSERIAL PRIMARY KEY,
      device_id TEXT UNIQUE NOT NULL,
      balance_coins BIGINT NOT NULL DEFAULT 0,
      best_score INTEGER NOT NULL DEFAULT 0,
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
      updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    )
  `);

  await pool.query(`
    CREATE TABLE IF NOT EXISTS public.player_accounts (
      id BIGSERIAL PRIMARY KEY,
      username TEXT UNIQUE NOT NULL,
      password_hash TEXT NOT NULL,
      auth_token_hash TEXT UNIQUE,
      device_id TEXT NOT NULL,
      balance_coins BIGINT NOT NULL DEFAULT 0,
      best_score INTEGER NOT NULL DEFAULT 0,
      last_daily_bonus DATE,
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
      updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    )
  `);

  await pool.query(`CREATE INDEX IF NOT EXISTS idx_player_accounts_token ON public.player_accounts(auth_token_hash)`);
  console.log("CoinRushIndia account tables ready");
}

async function accountFromToken(req) {
  const header = req.headers.authorization || "";
  if (!header.startsWith("Bearer ")) return null;
  const token = header.slice(7).trim();
  if (!token) return null;
  const result = await pool.query(
    `SELECT id, username, device_id, balance_coins, best_score, last_daily_bonus
     FROM public.player_accounts WHERE auth_token_hash = $1 LIMIT 1`,
    [sha256(token)]
  );
  return result.rows[0] || null;
}

app.get("/", (req, res) => res.json({ success: true, message: "CoinRushIndia Backend is running" }));
app.get("/api/v1/health", (req, res) => res.json({ success: true, status: "OK" }));
app.get("/api/v1/db-health", async (req, res) => {
  try { await pool.query("SELECT 1"); res.json({ success: true, database: "connected" }); }
  catch (error) { console.error(error.message); res.status(500).json({ success: false, database: "disconnected" }); }
});

// --------------------------------------------------
// REGISTER
// --------------------------------------------------
app.post("/api/v1/auth/register", async (req, res) => {
  try {
    const { username, password, device_id } = req.body;
    const user = String(username || "").trim();
    if (!validUsername(user) || !validPassword(password) || !validDevice(device_id)) {
      return res.status(400).json({ success: false, error: "Invalid account details" });
    }

    const exists = await pool.query(`SELECT id FROM public.player_accounts WHERE LOWER(username)=LOWER($1) LIMIT 1`, [user]);
    if (exists.rows.length) return res.status(409).json({ success: false, error: "Username already exists" });

    const deviceId = device_id.trim();
    const legacy = await pool.query(`SELECT balance_coins, best_score FROM public.player_balances WHERE device_id=$1 LIMIT 1`, [deviceId]);
    const balance = legacy.rows.length ? legacy.rows[0].balance_coins : 0;
    const best = legacy.rows.length ? legacy.rows[0].best_score : 0;

    const token = makeToken();
    const result = await pool.query(
      `INSERT INTO public.player_accounts
       (username,password_hash,auth_token_hash,device_id,balance_coins,best_score)
       VALUES ($1,$2,$3,$4,$5,$6)
       RETURNING id,username,device_id,balance_coins,best_score`,
      [user, sha256(password), sha256(token), deviceId, balance, best]
    );

    res.json({ success: true, token, daily_bonus_claimed_today: dailyClaimedToday(result.rows[0]), user: responseUser(result.rows[0]) });
  } catch (error) {
    console.error("Register error:", error.message);
    res.status(500).json({ success: false, error: "Unable to create account" });
  }
});

// --------------------------------------------------
// LOGIN
// --------------------------------------------------
app.post("/api/v1/auth/login", async (req, res) => {
  try {
    const { username, password, device_id } = req.body;
    const user = String(username || "").trim();
    if (!validUsername(user) || !validPassword(password) || !validDevice(device_id)) {
      return res.status(400).json({ success: false, error: "Invalid account details" });
    }

    let result = await pool.query(`SELECT * FROM public.player_accounts WHERE LOWER(username)=LOWER($1) LIMIT 1`, [user]);
    let account;

    if (result.rows.length === 0) {
      // One-time migration for the existing local CoinRushIndia account.
      const deviceId = device_id.trim();
      const legacy = await pool.query(`SELECT balance_coins,best_score FROM public.player_balances WHERE device_id=$1 LIMIT 1`, [deviceId]);
      const balance = legacy.rows.length ? legacy.rows[0].balance_coins : 0;
      const best = legacy.rows.length ? legacy.rows[0].best_score : 0;
      const token = makeToken();
      result = await pool.query(
        `INSERT INTO public.player_accounts
         (username,password_hash,auth_token_hash,device_id,balance_coins,best_score)
         VALUES ($1,$2,$3,$4,$5,$6) RETURNING *`,
        [user, sha256(password), sha256(token), deviceId, balance, best]
      );
      account = result.rows[0];
      return res.json({ success: true, token, daily_bonus_claimed_today: dailyClaimedToday(account), user: responseUser(account) });
    }

    account = result.rows[0];
    if (sha256(password) !== account.password_hash) {
      return res.status(401).json({ success: false, error: "Invalid username or password" });
    }

    const token = makeToken();
    const updated = await pool.query(
      `UPDATE public.player_accounts
       SET auth_token_hash=$1, device_id=$2, updated_at=NOW()
       WHERE id=$3
       RETURNING id,username,device_id,balance_coins,best_score,last_daily_bonus`,
      [sha256(token), device_id.trim(), account.id]
    );

    res.json({ success: true, token, daily_bonus_claimed_today: dailyClaimedToday(updated.rows[0]), user: responseUser(updated.rows[0]) });
  } catch (error) {
    console.error("Login error:", error.message);
    res.status(500).json({ success: false, error: "Unable to login" });
  }
});

// --------------------------------------------------
// RESET PASSWORD - requires the same device currently saved locally
// --------------------------------------------------
app.post("/api/v1/auth/reset", async (req, res) => {
  try {
    const { username, new_password, device_id } = req.body;
    const user = String(username || "").trim();
    if (!validUsername(user) || !validPassword(new_password) || !validDevice(device_id)) {
      return res.status(400).json({ success: false, error: "Invalid reset details" });
    }

    const result = await pool.query(`SELECT * FROM public.player_accounts WHERE LOWER(username)=LOWER($1) LIMIT 1`, [user]);
    if (result.rows.length === 0) {
      // If this is the old local account, reset also creates its cloud account.
      const legacy = await pool.query(`SELECT balance_coins,best_score FROM public.player_balances WHERE device_id=$1 LIMIT 1`, [device_id.trim()]);
      const balance = legacy.rows.length ? legacy.rows[0].balance_coins : 0;
      const best = legacy.rows.length ? legacy.rows[0].best_score : 0;
      const token = makeToken();
      const created = await pool.query(
        `INSERT INTO public.player_accounts
         (username,password_hash,auth_token_hash,device_id,balance_coins,best_score)
         VALUES ($1,$2,$3,$4,$5,$6) RETURNING id,username,device_id,balance_coins,best_score`,
        [user, sha256(new_password), sha256(token), device_id.trim(), balance, best]
      );
      return res.json({ success: true, token, daily_bonus_claimed_today: dailyClaimedToday(created.rows[0]), user: responseUser(created.rows[0]) });
    }

    const account = result.rows[0];
    if (account.device_id !== device_id.trim()) {
      return res.status(403).json({ success: false, error: "Account is linked to a different device" });
    }

    const token = makeToken();
    const updated = await pool.query(
      `UPDATE public.player_accounts SET password_hash=$1,auth_token_hash=$2,updated_at=NOW()
       WHERE id=$3 RETURNING id,username,device_id,balance_coins,best_score,last_daily_bonus`,
      [sha256(new_password), sha256(token), account.id]
    );
    res.json({ success: true, token, daily_bonus_claimed_today: dailyClaimedToday(updated.rows[0]), user: responseUser(updated.rows[0]) });
  } catch (error) {
    console.error("Reset error:", error.message);
    res.status(500).json({ success: false, error: "Unable to reset password" });
  }
});

app.post("/api/v1/auth/me", async (req, res) => {
  try {
    const account = await accountFromToken(req);
    if (!account) return res.status(401).json({ success: false, error: "Session expired" });
    await pool.query(`UPDATE public.player_accounts SET updated_at=NOW() WHERE id=$1`, [account.id]);
    res.json({ success: true, daily_bonus_claimed_today: dailyClaimedToday(account), user: responseUser(account) });
  } catch (error) {
    console.error("Session error:", error.message);
    res.status(500).json({ success: false, error: "Unable to verify session" });
  }
});

app.post("/api/v1/auth/logout", async (req, res) => {
  try {
    const account = await accountFromToken(req);
    if (account) await pool.query(`UPDATE public.player_accounts SET auth_token_hash=NULL,updated_at=NOW() WHERE id=$1`, [account.id]);
    res.json({ success: true });
  } catch (error) { res.json({ success: true }); }
});

// --------------------------------------------------
// DAILY BONUS - server enforces once per calendar day
// --------------------------------------------------
app.post("/api/v1/bonus/daily", async (req, res) => {
  try {
    const account = await accountFromToken(req);
    if (!account) return res.status(401).json({ success: false, error: "Session expired" });

    const result = await pool.query(
      `UPDATE public.player_accounts
       SET balance_coins=balance_coins+100,last_daily_bonus=CURRENT_DATE,updated_at=NOW()
       WHERE id=$1 AND (last_daily_bonus IS NULL OR last_daily_bonus <> CURRENT_DATE)
       RETURNING id,username,device_id,balance_coins,best_score,last_daily_bonus`,
      [account.id]
    );
    if (!result.rows.length) return res.status(409).json({ success: false, error: "Daily bonus already claimed" });

    await pool.query(
      `INSERT INTO public.player_balances(device_id,balance_coins,best_score)
       VALUES($1,$2,$3)
       ON CONFLICT(device_id) DO UPDATE SET balance_coins=$2,best_score=$3,updated_at=NOW()`,
      [result.rows[0].device_id, result.rows[0].balance_coins, result.rows[0].best_score]
    );
    res.json({ success: true, daily_bonus_claimed_today: true, user: responseUser(result.rows[0]) });
  } catch (error) {
    console.error("Daily bonus error:", error.message);
    res.status(500).json({ success: false, error: "Unable to claim daily bonus" });
  }
});

// --------------------------------------------------
// ADD COINS - authenticated account balance
// --------------------------------------------------
app.post("/api/v1/coins/add", async (req, res) => {
  try {
    const account = await accountFromToken(req);
    if (!account) return res.status(401).json({ success: false, error: "Session expired" });
    const coins = req.body.coins;
    if (!Number.isInteger(coins) || coins <= 0 || coins > 100000) return res.status(400).json({ success: false, error: "Invalid coins amount" });

    const result = await pool.query(
      `UPDATE public.player_accounts SET balance_coins=balance_coins+$1,updated_at=NOW()
       WHERE id=$2 RETURNING id,username,device_id,balance_coins,best_score,last_daily_bonus`,
      [coins, account.id]
    );
    const user = result.rows[0];
    await pool.query(
      `INSERT INTO public.player_balances(device_id,balance_coins,best_score)
       VALUES($1,$2,$3)
       ON CONFLICT(device_id) DO UPDATE SET balance_coins=$2,best_score=$3,updated_at=NOW()`,
      [user.device_id, user.balance_coins, user.best_score]
    );
    res.json({ success: true, daily_bonus_claimed_today: dailyClaimedToday(user), user: responseUser(user) });
  } catch (error) {
    console.error("Add coins error:", error.message);
    res.status(500).json({ success: false, error: "Unable to add coins" });
  }
});

app.post("/api/v1/score", async (req, res) => {
  try {
    const account = await accountFromToken(req);
    if (!account) return res.status(401).json({ success: false, error: "Session expired" });
    const score = req.body.score;
    if (!Number.isInteger(score) || score < 0 || score > 100000) return res.status(400).json({ success: false, error: "Invalid score" });
    const result = await pool.query(
      `UPDATE public.player_accounts SET best_score=GREATEST(best_score,$1),updated_at=NOW()
       WHERE id=$2 RETURNING id,username,device_id,balance_coins,best_score,last_daily_bonus`,
      [score, account.id]
    );
    const user = result.rows[0];
    await pool.query(`INSERT INTO public.player_balances(device_id,balance_coins,best_score) VALUES($1,$2,$3)
      ON CONFLICT(device_id) DO UPDATE SET balance_coins=$2,best_score=$3,updated_at=NOW()`,
      [user.device_id,user.balance_coins,user.best_score]);
    res.json({ success: true, daily_bonus_claimed_today: dailyClaimedToday(user), user: responseUser(user) });
  } catch (error) {
    console.error("Score error:", error.message);
    res.status(500).json({ success: false, error: "Unable to save score" });
  }
});

const PORT = process.env.PORT || 3000;
ensureTables().then(() => {
  app.listen(PORT, "0.0.0.0", () => console.log(`CoinRushIndia API running on port ${PORT}`));
}).catch(error => {
  console.error("Database initialization failed:", error.message);
  process.exit(1);
});
