const express = require("express");
const cors = require("cors");
const helmet = require("helmet");
const rateLimit = require("express-rate-limit");
const pool = require("./db");
require("dotenv").config();

const app = express();

app.set("trust proxy", 1);

app.use(helmet());
app.use(cors());
app.use(express.json({ limit: "20kb" }));

const limiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 100,
  standardHeaders: true,
  legacyHeaders: false
});

app.use("/api/", limiter);

// --------------------------------------------------
// HEALTH
// --------------------------------------------------

app.get("/", (req, res) => {
  res.json({
    success: true,
    message: "CoinRushIndia Backend is running"
  });
});

app.get("/api/v1/health", (req, res) => {
  res.json({
    success: true,
    status: "OK"
  });
});

app.get("/api/v1/db-health", async (req, res) => {
  try {
    await pool.query("SELECT 1");

    res.json({
      success: true,
      database: "connected"
    });
  } catch (error) {
    console.error("Database health check failed:", error.message);

    res.status(500).json({
      success: false,
      database: "disconnected"
    });
  }
});

// --------------------------------------------------
// CREATE COIN TABLE
// --------------------------------------------------

async function createCoinTable() {
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

  console.log("player_balances table ready");
}

// --------------------------------------------------
// GET / CREATE PLAYER
// --------------------------------------------------

app.post("/api/v1/user", async (req, res) => {
  try {
    const { device_id } = req.body;

    if (
      typeof device_id !== "string" ||
      device_id.trim().length < 6 ||
      device_id.trim().length > 200
    ) {
      return res.status(400).json({
        success: false,
        error: "Invalid device_id"
      });
    }

    const deviceId = device_id.trim();

    const result = await pool.query(
      `
      INSERT INTO public.player_balances (device_id)
      VALUES ($1)
      ON CONFLICT (device_id)
      DO UPDATE SET updated_at = NOW()
      RETURNING
        id,
        device_id,
        balance_coins,
        best_score
      `,
      [deviceId]
    );

    res.json({
      success: true,
      user: result.rows[0]
    });

  } catch (error) {
    console.error("User API error:", error.message);

    res.status(500).json({
      success: false,
      error: "Unable to create/find player"
    });
  }
});

// --------------------------------------------------
// GET BALANCE
// --------------------------------------------------

app.get("/api/v1/user/:device_id", async (req, res) => {
  try {
    const deviceId = req.params.device_id;

    const result = await pool.query(
      `
      SELECT
        id,
        device_id,
        balance_coins,
        best_score
      FROM public.player_balances
      WHERE device_id = $1
      LIMIT 1
      `,
      [deviceId]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({
        success: false,
        error: "Player not found"
      });
    }

    res.json({
      success: true,
      user: result.rows[0]
    });

  } catch (error) {
    console.error("Get balance error:", error.message);

    res.status(500).json({
      success: false,
      error: "Unable to get balance"
    });
  }
});

// --------------------------------------------------
// ADD COINS
// --------------------------------------------------

app.post("/api/v1/coins/add", async (req, res) => {
  try {
    const { device_id, coins } = req.body;

    if (
      typeof device_id !== "string" ||
      device_id.trim().length < 6 ||
      device_id.trim().length > 200
    ) {
      return res.status(400).json({
        success: false,
        error: "Invalid device_id"
      });
    }

    if (
      !Number.isInteger(coins) ||
      coins <= 0 ||
      coins > 100000
    ) {
      return res.status(400).json({
        success: false,
        error: "Invalid coins amount"
      });
    }

    const deviceId = device_id.trim();

    const result = await pool.query(
      `
      INSERT INTO public.player_balances
        (device_id, balance_coins)
      VALUES
        ($1, $2)
      ON CONFLICT (device_id)
      DO UPDATE SET
        balance_coins =
          public.player_balances.balance_coins + EXCLUDED.balance_coins,
        updated_at = NOW()
      RETURNING
        id,
        device_id,
        balance_coins,
        best_score
      `,
      [deviceId, coins]
    );

    res.json({
      success: true,
      user: result.rows[0]
    });

  } catch (error) {
    console.error("Add coins error:", error.message);

    res.status(500).json({
      success: false,
      error: "Unable to add coins"
    });
  }
});

// --------------------------------------------------
// SET BEST SCORE
// --------------------------------------------------

app.post("/api/v1/score", async (req, res) => {
  try {
    const { device_id, score } = req.body;

    if (
      typeof device_id !== "string" ||
      device_id.trim().length < 6
    ) {
      return res.status(400).json({
        success: false,
        error: "Invalid device_id"
      });
    }

    if (
      !Number.isInteger(score) ||
      score < 0 ||
      score > 100000
    ) {
      return res.status(400).json({
        success: false,
        error: "Invalid score"
      });
    }

    const result = await pool.query(
      `
      INSERT INTO public.player_balances
        (device_id, best_score)
      VALUES
        ($1, $2)
      ON CONFLICT (device_id)
      DO UPDATE SET
        best_score =
          GREATEST(
            public.player_balances.best_score,
            EXCLUDED.best_score
          ),
        updated_at = NOW()
      RETURNING
        id,
        device_id,
        balance_coins,
        best_score
      `,
      [device_id.trim(), score]
    );

    res.json({
      success: true,
      user: result.rows[0]
    });

  } catch (error) {
    console.error("Score API error:", error.message);

    res.status(500).json({
      success: false,
      error: "Unable to save score"
    });
  }
});

// --------------------------------------------------
// START SERVER
// --------------------------------------------------

const PORT = process.env.PORT || 3000;

createCoinTable()
  .then(() => {
    app.listen(PORT, "0.0.0.0", () => {
      console.log(
        `CoinRushIndia API running on port ${PORT}`
      );
    });
  })
  .catch((error) => {
    console.error(
      "Database initialization failed:",
      error.message
    );

    process.exit(1);
  });
