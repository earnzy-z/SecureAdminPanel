import type { Express, Request, Response } from "express";

export function setupAuthRoutes(app: Express) {
  app.get("/api/auth/user", (req: Request, res: Response) => {
    res.json({
      id: "admin-dev-001",
      email: "admin@example.com",
      firstName: "Admin",
      lastName: "Panel",
      profileImageUrl: undefined,
    });
  });

  app.post("/api/logout", (req: Request, res: Response) => {
    res.json({ success: true });
  });
}
