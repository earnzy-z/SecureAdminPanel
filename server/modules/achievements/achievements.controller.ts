import type { Express, Request, Response } from "express";
import { storage } from "../../storage";
import { insertAchievementSchema } from "@shared/schema";

export function setupAchievementsRoutes(app: Express) {
  app.get("/api/achievements", async (req: Request, res: Response) => {
    try {
      const achievements = await storage.getAllAchievements();
      res.json(achievements);
    } catch (error: any) {
      res.status(500).json({ error: error.message });
    }
  });

  app.post("/api/achievements", async (req: Request, res: Response) => {
    try {
      const data = insertAchievementSchema.parse(req.body);
      const achievement = await storage.createAchievement(data);
      res.json(achievement);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });

  app.patch("/api/achievements/:id", async (req: Request, res: Response) => {
    try {
      const achievement = await storage.updateAchievement(req.params.id, req.body);
      res.json(achievement);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });

  app.delete("/api/achievements/:id", async (req: Request, res: Response) => {
    try {
      await storage.deleteAchievement(req.params.id);
      res.json({ success: true });
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });

  app.post("/api/achievements/:id/toggle", async (req: Request, res: Response) => {
    try {
      const { isActive } = req.body;
      await storage.toggleAchievementActive(req.params.id, isActive);
      res.json({ success: true });
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });
}
