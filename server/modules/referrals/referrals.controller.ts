import type { Express, Request, Response } from "express";
import { storage } from "../../storage";

export function setupReferralsRoutes(app: Express) {
  app.get("/api/referrals", async (req: Request, res: Response) => {
    try {
      const referrals = await storage.getAllReferrals();
      res.json(referrals);
    } catch (error: any) {
      res.status(500).json({ error: error.message });
    }
  });
}
