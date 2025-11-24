import type { Express } from "express";
import { createServer, type Server } from "http";
import { storage } from "./storage";
import { 
  insertAppUserSchema, insertTransactionSchema, insertOfferSchema, insertBannerSchema,
  insertPromoCodeSchema, insertSupportTicketSchema, insertTicketMessageSchema,
  insertAchievementSchema, insertTaskSchema, insertWithdrawalSchema,
  insertNotificationSchema, insertAutoBanRuleSchema
} from "@shared/schema";
import { z } from "zod";
import { isAuthenticated } from "./authUtils";

export async function registerRoutes(app: Express): Promise<Server> {
  // Auth endpoints
  app.get("/api/auth/user", (req, res) => {
    const user = (req as any).user as any;
    if (!req.isAuthenticated() || !user) {
      return res.status(401).json({ message: "Unauthorized" });
    }
    res.json({
      id: user.claims?.sub || user.id,
      email: user.claims?.email || user.email,
      firstName: user.claims?.given_name,
      lastName: user.claims?.family_name,
      profileImageUrl: user.claims?.picture,
    });
  });

  app.post("/api/logout", (req, res) => {
    req.logout((err) => {
      if (err) return res.status(500).json({ error: err.message });
      res.json({ success: true });
    });
  });
  // Dashboard Stats
  app.get("/api/stats", async (req, res) => {
    const users = await storage.getAllUsers();
    const transactions = await storage.getAllTransactions();
    const withdrawals = await storage.getAllWithdrawals();

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
      activeTickets: 5, // Placeholder
    });
  });

  // Users
  app.get("/api/users", async (req, res) => {
    const users = await storage.getAllUsers();
    res.json(users);
  });

  app.get("/api/users/:id", async (req, res) => {
    const user = await storage.getUser(req.params.id);
    if (!user) return res.status(404).json({ error: "User not found" });
    res.json(user);
  });

  app.post("/api/users/:id/ban", async (req, res) => {
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

  // Transactions
  app.get("/api/transactions", async (req, res) => {
    const transactions = await storage.getAllTransactions();
    res.json(transactions);
  });

  // Coin Control
  app.post("/api/coins/adjust", async (req, res) => {
    try {
      const { userId, amount, description } = req.body;
      const user = await storage.getUser(userId);
      if (!user) return res.status(404).json({ error: "User not found" });

      // Update user coins
      await storage.updateUser(userId, {
        coins: user.coins + amount,
        totalEarned: amount > 0 ? user.totalEarned + amount : user.totalEarned,
      });

      // Create transaction
      await storage.createTransaction({
        userId,
        type: amount > 0 ? "bonus" : "spend",
        amount: Math.abs(amount),
        description,
        status: "completed",
      });

      res.json({ success: true });
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });

  app.post("/api/coins/bulk-credit", async (req, res) => {
    try {
      const { amount, description } = req.body;
      const users = await storage.getAllUsers();
      const activeUsers = users.filter(u => !u.isBanned);

      for (const user of activeUsers) {
        await storage.updateUser(user.id, {
          coins: user.coins + amount,
          totalEarned: user.totalEarned + amount,
        });

        await storage.createTransaction({
          userId: user.id,
          type: "bonus",
          amount,
          description,
          status: "completed",
        });
      }

      res.json({ success: true, count: activeUsers.length });
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });

  // Offers
  app.get("/api/offers", async (req, res) => {
    const offers = await storage.getAllOffers();
    res.json(offers);
  });

  app.post("/api/offers", async (req, res) => {
    try {
      const data = insertOfferSchema.parse(req.body);
      const offer = await storage.createOffer(data);
      res.json(offer);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });

  app.post("/api/offers/:id", async (req, res) => {
    try {
      const data = insertOfferSchema.partial().parse(req.body);
      const offer = await storage.updateOffer(req.params.id, data);
      if (!offer) return res.status(404).json({ error: "Offer not found" });
      res.json(offer);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });

  app.delete("/api/offers/:id", async (req, res) => {
    await storage.deleteOffer(req.params.id);
    res.json({ success: true });
  });

  app.post("/api/offers/:id/toggle", async (req, res) => {
    try {
      const { isActive } = req.body;
      await storage.toggleOfferActive(req.params.id, isActive);
      res.json({ success: true });
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });

  // Banners
  app.get("/api/banners", async (req, res) => {
    const banners = await storage.getAllBanners();
    res.json(banners);
  });

  app.post("/api/banners", async (req, res) => {
    try {
      const data = insertBannerSchema.parse(req.body);
      const banner = await storage.createBanner(data);
      res.json(banner);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });

  app.post("/api/banners/:id", async (req, res) => {
    try {
      const data = insertBannerSchema.partial().parse(req.body);
      const banner = await storage.updateBanner(req.params.id, data);
      if (!banner) return res.status(404).json({ error: "Banner not found" });
      res.json(banner);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });

  app.delete("/api/banners/:id", async (req, res) => {
    await storage.deleteBanner(req.params.id);
    res.json({ success: true });
  });

  app.post("/api/banners/:id/toggle", async (req, res) => {
    try {
      const { isActive } = req.body;
      await storage.toggleBannerActive(req.params.id, isActive);
      res.json({ success: true });
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });

  // Tasks
  app.get("/api/tasks", async (req, res) => {
    const tasks = await storage.getAllTasks();
    res.json(tasks);
  });

  app.post("/api/tasks", async (req, res) => {
    try {
      const data = insertTaskSchema.parse(req.body);
      const task = await storage.createTask(data);
      res.json(task);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });

  app.post("/api/tasks/:id", async (req, res) => {
    try {
      const data = insertTaskSchema.partial().parse(req.body);
      const task = await storage.updateTask(req.params.id, data);
      if (!task) return res.status(404).json({ error: "Task not found" });
      res.json(task);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });

  app.delete("/api/tasks/:id", async (req, res) => {
    await storage.deleteTask(req.params.id);
    res.json({ success: true });
  });

  app.post("/api/tasks/:id/toggle", async (req, res) => {
    try {
      const { isActive } = req.body;
      await storage.toggleTaskActive(req.params.id, isActive);
      res.json({ success: true });
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });

  // Achievements
  app.get("/api/achievements", async (req, res) => {
    const achievements = await storage.getAllAchievements();
    res.json(achievements);
  });

  app.post("/api/achievements", async (req, res) => {
    try {
      const data = insertAchievementSchema.parse(req.body);
      const achievement = await storage.createAchievement(data);
      res.json(achievement);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });

  app.post("/api/achievements/:id", async (req, res) => {
    try {
      const data = insertAchievementSchema.partial().parse(req.body);
      const achievement = await storage.updateAchievement(req.params.id, data);
      if (!achievement) return res.status(404).json({ error: "Achievement not found" });
      res.json(achievement);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });

  app.delete("/api/achievements/:id", async (req, res) => {
    await storage.deleteAchievement(req.params.id);
    res.json({ success: true });
  });

  app.post("/api/achievements/:id/toggle", async (req, res) => {
    try {
      const { isActive } = req.body;
      await storage.toggleAchievementActive(req.params.id, isActive);
      res.json({ success: true });
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });

  // Notifications
  app.get("/api/notifications", async (req, res) => {
    const notifications = await storage.getAllNotifications();
    res.json(notifications);
  });

  app.post("/api/notifications/send", async (req, res) => {
    try {
      const data = insertNotificationSchema.parse(req.body);
      const notification = await storage.createNotification(data);
      res.json(notification);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });

  // Promo Codes
  app.get("/api/promo-codes", async (req, res) => {
    const promoCodes = await storage.getAllPromoCodes();
    res.json(promoCodes);
  });

  app.post("/api/promo-codes", async (req, res) => {
    try {
      const data = insertPromoCodeSchema.parse(req.body);
      const promoCode = await storage.createPromoCode(data);
      res.json(promoCode);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });

  app.delete("/api/promo-codes/:id", async (req, res) => {
    await storage.deletePromoCode(req.params.id);
    res.json({ success: true });
  });

  app.post("/api/promo-codes/:id/toggle", async (req, res) => {
    try {
      const { isActive } = req.body;
      await storage.togglePromoCodeActive(req.params.id, isActive);
      res.json({ success: true });
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });

  // Support Tickets
  app.get("/api/support/tickets", async (req, res) => {
    const tickets = await storage.getAllSupportTickets();
    res.json(tickets);
  });

  app.get("/api/support/tickets/:id/messages", async (req, res) => {
    const messages = await storage.getTicketMessages(req.params.id);
    res.json(messages);
  });

  app.post("/api/support/tickets/:id/messages", async (req, res) => {
    try {
      const data = insertTicketMessageSchema.parse({
        ...req.body,
        ticketId: req.params.id,
      });
      const message = await storage.createTicketMessage(data);
      res.json(message);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });

  app.post("/api/support/tickets/:id/status", async (req, res) => {
    try {
      const { status } = req.body;
      await storage.updateTicketStatus(req.params.id, status);
      res.json({ success: true });
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });

  // Leaderboard
  app.get("/api/leaderboard", async (req, res) => {
    const leaderboard = await storage.getAllLeaderboard();
    res.json(leaderboard);
  });

  // Referrals
  app.get("/api/referrals", async (req, res) => {
    const referrals = await storage.getAllReferrals();
    res.json(referrals);
  });

  // Withdrawals
  app.get("/api/withdrawals", async (req, res) => {
    const withdrawals = await storage.getAllWithdrawals();
    res.json(withdrawals);
  });

  app.post("/api/withdrawals/:id/process", async (req, res) => {
    try {
      const { status, adminNote } = req.body;
      await storage.updateWithdrawalStatus(req.params.id, status, adminNote);
      res.json({ success: true });
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });

  // Auto-Ban Rules
  app.get("/api/auto-ban-rules", async (req, res) => {
    const rules = await storage.getAllAutoBanRules();
    res.json(rules);
  });

  app.post("/api/auto-ban-rules", async (req, res) => {
    try {
      const data = insertAutoBanRuleSchema.parse(req.body);
      const rule = await storage.createAutoBanRule(data);
      res.json(rule);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });

  app.delete("/api/auto-ban-rules/:id", async (req, res) => {
    await storage.deleteAutoBanRule(req.params.id);
    res.json({ success: true });
  });

  app.post("/api/auto-ban-rules/:id/toggle", async (req, res) => {
    try {
      const { isActive } = req.body;
      await storage.toggleAutoBanRuleActive(req.params.id, isActive);
      res.json({ success: true });
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });

  const httpServer = createServer(app);

  return httpServer;
}
