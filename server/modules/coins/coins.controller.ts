import type { Express, Request, Response } from "express";
import { storage } from "../../storage";

export function setupCoinsRoutes(app: Express) {
  app.post("/api/coins/adjust", async (req: Request, res: Response) => {
    try {
      const { userId, amount, description } = req.body;
      const user = await storage.getUser(userId);
      if (!user) return res.status(404).json({ error: "User not found" });

      const newCoins = Math.max(0, user.coins + amount);
      await storage.updateUser(userId, { coins: newCoins });

      await storage.createTransaction({
        userId,
        type: amount > 0 ? "bonus" : "spend",
        amount: Math.abs(amount),
        description: description || "Coin adjustment",
        status: "completed",
      });

      res.json({ success: true, newCoins });
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });

  app.get("/api/coins/history/:userId", async (req: Request, res: Response) => {
    try {
      const transactions = await storage.getAllTransactions();
      const userTransactions = transactions.filter(t => t.userId === req.params.userId);
      res.json(userTransactions);
    } catch (error: any) {
      res.status(500).json({ error: error.message });
    }
  });
}
