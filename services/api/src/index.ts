import express from "express";
import cors from "cors";
import helmet from "helmet";
import { setupAuthRoutes } from "./routes/auth.routes";
import { setupUsersRoutes } from "./routes/users.routes";
import { setupOffersRoutes } from "./routes/offers.routes";

const app = express();
const PORT = process.env.API_PORT || 3000;

// Middleware
app.use(helmet());
app.use(cors({
  origin: process.env.ADMIN_URL || "http://localhost:3000",
  credentials: true,
}));
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Health check
app.get("/health", (req, res) => {
  res.json({ status: "ok", timestamp: new Date().toISOString() });
});

// Register all routes
setupAuthRoutes(app);
setupUsersRoutes(app);
setupOffersRoutes(app);

// 404 handler
app.use((req, res) => {
  res.status(404).json({ error: "Not found" });
});

// Error handler
app.use((err: any, req: express.Request, res: express.Response, next: express.NextFunction) => {
  console.error(err);
  res.status(500).json({ error: "Internal server error" });
});

// Start server
if (require.main === module) {
  app.listen(PORT, "0.0.0.0", () => {
    console.log(`✅ API running on port ${PORT}`);
  });
}

export { app };
