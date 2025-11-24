import type { Express, Request, Response } from "express";
import { createServer, type Server } from "http";
import { storage } from "./storage";
import { registerModules } from "./modules";

declare global {
  namespace Express {
    interface Request {
      isAuthenticated?(): boolean;
      logout?(callback: (err?: any) => void): void;
    }
  }
}

export async function registerRoutes(app: Express): Promise<Server> {
  // Register all modular routes
  registerModules(app);

  // Dashboard Stats endpoint
  app.get("/api/stats", async (req: Request, res: Response) => {
    try {
      const users = await storage.getAllUsers();
      const transactions = await storage.getAllTransactions();
      const withdrawals = await storage.getAllWithdrawals();
      const supportTickets = await storage.getAllSupportTickets();

      res.json({
        totalUsers: users.length,
        activeUsers: users.filter(u => !u.isBanned).length,
        inactiveUsers: users.filter(u => !u.isBanned && u.coins === 0).length,
        bannedUsers: users.filter(u => u.isBanned).length,
        totalTransactions: transactions.length,
        totalCoins: users.reduce((sum, u) => sum + u.coins, 0),
        pendingWithdrawals: withdrawals.filter(w => w.status === "pending").length,
        todaySignups: users.filter(u => 
          new Date(u.createdAt).toDateString() === new Date().toDateString()
        ).length,
        todayCoins: transactions.filter(t => 
          new Date(t.createdAt).toDateString() === new Date().toDateString()
        ).reduce((sum, t) => sum + t.amount, 0),
        activeTickets: supportTickets.filter(t => t.status === "open" || t.status === "in_progress").length,
      });
    } catch (error: any) {
      res.status(500).json({ error: error.message });
    }
  });

  const httpServer = createServer(app);
  return httpServer;
}
