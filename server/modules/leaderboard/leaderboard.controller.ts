import type { Express, Request, Response } from "express";
import { storage } from "../../storage";

export function setupLeaderboardRoutes(app: Express) {
  app.get("/api/leaderboard", async (req: Request, res: Response) => {
    try {
      const leaderboard = await storage.getAllLeaderboard();
      const sorted = leaderboard.sort((a, b) => b.totalCoins - a.totalCoins);
      res.json(sorted);
    } catch (error: any) {
      res.status(500).json({ error: error.message });
    }
  });

  app.post("/api/leaderboard/:userId", async (req: Request, res: Response) => {
    try {
      const { totalCoins } = req.body;
      await storage.updateLeaderboard(req.params.userId, totalCoins);
      res.json({ success: true });
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });
}
