import type { Express, Request, Response } from "express";
import { storage } from "../../storage";

export function setupWithdrawalsRoutes(app: Express) {
  app.get("/api/withdrawals", async (req: Request, res: Response) => {
    try {
      const withdrawals = await storage.getAllWithdrawals();
      res.json(withdrawals);
    } catch (error: any) {
      res.status(500).json({ error: error.message });
    }
  });

  app.post("/api/withdrawals/:id/approve", async (req: Request, res: Response) => {
    try {
      const { adminNote } = req.body;
      await storage.updateWithdrawalStatus(req.params.id, "approved", adminNote);
      res.json({ success: true });
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });

  app.post("/api/withdrawals/:id/reject", async (req: Request, res: Response) => {
    try {
      const { adminNote } = req.body;
      await storage.updateWithdrawalStatus(req.params.id, "rejected", adminNote);
      res.json({ success: true });
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });
}
