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

// Basic health
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

// Database health
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

// Create user or return existing user
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
      INSERT INTO public.users (device_id)
      VALUES ($1)
      ON CONFLICT (device_id)
      DO UPDATE SET updated_at = NOW()
      RETURNING id, device_id, balance_coins, best_score
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
      error: "Unable to create/find user"
    });
  }
});

// Get user's current coin balance
app.get("/api/v1/user/:device_id", async (req, res) => {
  try {
    const deviceId = req.params.device_id;

    const result = await pool.query(
      `
      SELECT id, device_id, balance_coins, best_score
      FROM public.users
      WHERE device_id = $1
      LIMIT 1
      `,
      [deviceId]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({
        success: false,
        error: "User not found"
      });
    }

    res.json({
      success: true,
      user: result.rows[0]
    });
  } catch (error) {
    console.error("Get user error:", error.message);

    res.status(500).json({
      success: false,
      error: "Unable to get user"
    });
  }
});

// Add coins to user's balance
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
      UPDATE public.users
      SET
        balance_coins = balance_coins + $1,
        updated_at = NOW()
      WHERE device_id = $2
      RETURNING id, device_id, balance_coins, best_score
      `,
      [coins, deviceId]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({
        success: false,
        error: "User not found"
      });
    }

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

const PORT = process.env.PORT || 3000;

app.listen(PORT, "0.0.0.0", () => {
  console.log(`CoinRushIndia API running on port ${PORT}`);
});
