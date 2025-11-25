import type { Express } from "express";
import jwt from "jsonwebtoken";

export function setupAuthRoutes(app: Express) {
  app.post("/api/auth/login", (req, res) => {
    const { email, password } = req.body;

    // Mock auth - replace with real implementation
    if (email && password) {
      const token = jwt.sign(
        { id: "admin-001", email, role: "ADMIN" },
        process.env.JWT_SECRET || "dev-secret",
        { expiresIn: "24h" }
      );

      res.json({ token, admin: { id: "admin-001", email, role: "ADMIN" } });
    } else {
      res.status(400).json({ error: "Invalid credentials" });
    }
  });

  app.get("/api/auth/user", (req, res) => {
    // Mock authenticated user
    res.json({
      id: "admin-001",
      email: "admin@earnzy.com",
      firstName: "Admin",
      lastName: "Panel",
      role: "ADMIN",
      profileImageUrl: null,
    });
  });

  app.post("/api/logout", (req, res) => {
    res.json({ success: true });
  });
}
