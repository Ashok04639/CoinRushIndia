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
const PORT = process.env.PORT || 3000;

 app.listen(PORT, "0.0.0.0", () => {
  console.log(`CoinRushIndia API running on port ${PORT}`);
});
