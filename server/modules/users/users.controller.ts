import type { Express, Request, Response } from "express";
import { storage } from "../../storage";

export function setupUsersRoutes(app: Express) {
  app.get("/api/users", async (req: Request, res: Response) => {
    try {
      const users = await storage.getAllUsers();
      res.json(users);
    } catch (error: any) {
      res.status(500).json({ error: error.message });
    }
  });

  app.get("/api/users/:id", async (req: Request, res: Response) => {
    try {
      const user = await storage.getUser(req.params.id);
      if (!user) return res.status(404).json({ error: "User not found" });
      res.json(user);
    } catch (error: any) {
      res.status(500).json({ error: error.message });
    }
  });

  app.post("/api/users/:id/ban", async (req: Request, res: Response) => {
    try {
      const { reason, ban } = req.body;
      if (ban) {
        await storage.banUser(req.params.id, reason);
      } else {
        await storage.unbanUser(req.params.id);
      }
      res.json({ success: true });
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });
}
