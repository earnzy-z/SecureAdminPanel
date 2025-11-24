import {
  type User, type InsertUser,
  type Transaction, type InsertTransaction,
  type Offer, type InsertOffer,
  type Banner, type InsertBanner,
  type PromoCode, type InsertPromoCode,
  type SupportTicket, type InsertSupportTicket,
  type TicketMessage, type InsertTicketMessage,
  type Achievement, type InsertAchievement,
  type UserAchievement, type InsertUserAchievement,
  type Task, type InsertTask,
  type Withdrawal, type InsertWithdrawal,
  type Referral, type InsertReferral,
  type Leaderboard, type InsertLeaderboard,
  type Notification, type InsertNotification,
  type AutoBanRule, type InsertAutoBanRule,
} from "@shared/schema";
import { randomUUID } from "crypto";

export interface IStorage {
  // Users
  getAllUsers(): Promise<User[]>;
  getUser(id: string): Promise<User | undefined>;
  getUserByUsername(username: string): Promise<User | undefined>;
  createUser(user: InsertUser): Promise<User>;
  updateUser(id: string, updates: Partial<User>): Promise<User | undefined>;
  banUser(id: string, reason?: string): Promise<void>;
  unbanUser(id: string): Promise<void>;
  
  // Transactions
  getAllTransactions(): Promise<Transaction[]>;
  getTransaction(id: string): Promise<Transaction | undefined>;
  createTransaction(transaction: InsertTransaction): Promise<Transaction>;
  
  // Offers
  getAllOffers(): Promise<Offer[]>;
  getOffer(id: string): Promise<Offer | undefined>;
  createOffer(offer: InsertOffer): Promise<Offer>;
  updateOffer(id: string, updates: Partial<Offer>): Promise<Offer | undefined>;
  deleteOffer(id: string): Promise<void>;
  toggleOfferActive(id: string, isActive: boolean): Promise<void>;
  
  // Banners
  getAllBanners(): Promise<Banner[]>;
  getBanner(id: string): Promise<Banner | undefined>;
  createBanner(banner: InsertBanner): Promise<Banner>;
  updateBanner(id: string, updates: Partial<Banner>): Promise<Banner | undefined>;
  deleteBanner(id: string): Promise<void>;
  toggleBannerActive(id: string, isActive: boolean): Promise<void>;
  
  // Promo Codes
  getAllPromoCodes(): Promise<PromoCode[]>;
  getPromoCode(id: string): Promise<PromoCode | undefined>;
  getPromoCodeByCode(code: string): Promise<PromoCode | undefined>;
  createPromoCode(promoCode: InsertPromoCode): Promise<PromoCode>;
  deletePromoCode(id: string): Promise<void>;
  togglePromoCodeActive(id: string, isActive: boolean): Promise<void>;
  
  // Support Tickets
  getAllSupportTickets(): Promise<SupportTicket[]>;
  getSupportTicket(id: string): Promise<SupportTicket | undefined>;
  createSupportTicket(ticket: InsertSupportTicket): Promise<SupportTicket>;
  updateTicketStatus(id: string, status: string): Promise<void>;
  getTicketMessages(ticketId: string): Promise<TicketMessage[]>;
  createTicketMessage(message: InsertTicketMessage): Promise<TicketMessage>;
  
  // Achievements
  getAllAchievements(): Promise<Achievement[]>;
  getAchievement(id: string): Promise<Achievement | undefined>;
  createAchievement(achievement: InsertAchievement): Promise<Achievement>;
  updateAchievement(id: string, updates: Partial<Achievement>): Promise<Achievement | undefined>;
  deleteAchievement(id: string): Promise<void>;
  toggleAchievementActive(id: string, isActive: boolean): Promise<void>;
  
  // Tasks
  getAllTasks(): Promise<Task[]>;
  getTask(id: string): Promise<Task | undefined>;
  createTask(task: InsertTask): Promise<Task>;
  updateTask(id: string, updates: Partial<Task>): Promise<Task | undefined>;
  deleteTask(id: string): Promise<void>;
  toggleTaskActive(id: string, isActive: boolean): Promise<void>;
  
  // Withdrawals
  getAllWithdrawals(): Promise<Withdrawal[]>;
  getWithdrawal(id: string): Promise<Withdrawal | undefined>;
  createWithdrawal(withdrawal: InsertWithdrawal): Promise<Withdrawal>;
  updateWithdrawalStatus(id: string, status: string, adminNote?: string): Promise<void>;
  
  // Referrals
  getAllReferrals(): Promise<Referral[]>;
  getReferral(id: string): Promise<Referral | undefined>;
  createReferral(referral: InsertReferral): Promise<Referral>;
  
  // Leaderboard
  getAllLeaderboard(): Promise<Leaderboard[]>;
  updateLeaderboard(userId: string, totalCoins: number): Promise<void>;
  
  // Notifications
  getAllNotifications(): Promise<Notification[]>;
  getNotification(id: string): Promise<Notification | undefined>;
  createNotification(notification: InsertNotification): Promise<Notification>;
  
  // Auto-Ban Rules
  getAllAutoBanRules(): Promise<AutoBanRule[]>;
  getAutoBanRule(id: string): Promise<AutoBanRule | undefined>;
  createAutoBanRule(rule: InsertAutoBanRule): Promise<AutoBanRule>;
  deleteAutoBanRule(id: string): Promise<void>;
  toggleAutoBanRuleActive(id: string, isActive: boolean): Promise<void>;
}

export class MemStorage implements IStorage {
  private users: Map<string, User> = new Map();
  private transactions: Map<string, Transaction> = new Map();
  private offers: Map<string, Offer> = new Map();
  private banners: Map<string, Banner> = new Map();
  private promoCodes: Map<string, PromoCode> = new Map();
  private supportTickets: Map<string, SupportTicket> = new Map();
  private ticketMessages: Map<string, TicketMessage> = new Map();
  private achievements: Map<string, Achievement> = new Map();
  private tasks: Map<string, Task> = new Map();
  private withdrawals: Map<string, Withdrawal> = new Map();
  private referrals: Map<string, Referral> = new Map();
  private leaderboard: Map<string, Leaderboard> = new Map();
  private notifications: Map<string, Notification> = new Map();
  private autoBanRules: Map<string, AutoBanRule> = new Map();

  constructor() {
    this.seedData();
  }

  private seedData() {
    // Seed some sample users
    for (let i = 1; i <= 20; i++) {
      const id = randomUUID();
      const user: User = {
        id,
        username: `user${i}`,
        email: `user${i}@example.com`,
        coins: Math.floor(Math.random() * 5000) + 100,
        totalEarned: Math.floor(Math.random() * 10000) + 500,
        isBanned: i > 18,
        banReason: i > 18 ? "Suspicious activity detected" : null,
        deviceToken: null,
        referralCode: `REF${i.toString().padStart(4, '0')}`,
        referredBy: null,
        createdAt: new Date(Date.now() - Math.random() * 30 * 24 * 60 * 60 * 1000),
      };
      this.users.set(id, user);

      // Create transactions for each user
      const txnTypes = ["earn", "spend", "bonus", "referral"];
      for (let j = 0; j < 5; j++) {
        const txnId = randomUUID();
        const type = txnTypes[Math.floor(Math.random() * txnTypes.length)];
        const transaction: Transaction = {
          id: txnId,
          userId: id,
          type,
          amount: Math.floor(Math.random() * 500) + 10,
          description: `${type.charAt(0).toUpperCase() + type.slice(1)} - ${Math.random() > 0.5 ? 'Daily task' : 'Offer completion'}`,
          status: Math.random() > 0.1 ? "completed" : "pending",
          createdAt: new Date(Date.now() - Math.random() * 20 * 24 * 60 * 60 * 1000),
        };
        this.transactions.set(txnId, transaction);
      }
    }

    // Seed offers
    const offerCategories = ["survey", "app", "video", "other"];
    for (let i = 1; i <= 8; i++) {
      const id = randomUUID();
      const offer: Offer = {
        id,
        title: `Offer ${i} - ${offerCategories[i % offerCategories.length]}`,
        description: `Complete this ${offerCategories[i % offerCategories.length]} to earn coins!`,
        coins: Math.floor(Math.random() * 200) + 50,
        imageUrl: `https://picsum.photos/seed/offer${i}/400/300`,
        actionUrl: `https://example.com/offer/${i}`,
        category: offerCategories[i % offerCategories.length],
        isActive: i <= 6,
        priority: i,
        createdAt: new Date(),
      };
      this.offers.set(id, offer);
    }

    // Seed withdrawals
    const userIds = Array.from(this.users.keys());
    for (let i = 0; i < 5; i++) {
      const id = randomUUID();
      const statuses = ["pending", "approved", "rejected", "completed"];
      const withdrawal: Withdrawal = {
        id,
        userId: userIds[i],
        amount: Math.floor(Math.random() * 100) + 10,
        method: i % 2 === 0 ? "paypal" : "bank",
        accountDetails: i % 2 === 0 ? `user${i}@paypal.com` : `Bank Account ****${1000 + i}`,
        status: statuses[Math.floor(Math.random() * statuses.length)],
        adminNote: null,
        createdAt: new Date(Date.now() - Math.random() * 10 * 24 * 60 * 60 * 1000),
        processedAt: null,
      };
      this.withdrawals.set(id, withdrawal);
    }

    // Seed leaderboard
    userIds.slice(0, 10).forEach((userId, idx) => {
      const user = this.users.get(userId)!;
      const id = randomUUID();
      this.leaderboard.set(userId, {
        id,
        userId,
        totalCoins: user.totalEarned,
        rank: idx + 1,
        updatedAt: new Date(),
      });
    });
  }

  // Users
  async getAllUsers(): Promise<User[]> {
    return Array.from(this.users.values()).sort((a, b) => 
      new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    );
  }

  async getUser(id: string): Promise<User | undefined> {
    return this.users.get(id);
  }

  async getUserByUsername(username: string): Promise<User | undefined> {
    return Array.from(this.users.values()).find(u => u.username === username);
  }

  async createUser(insertUser: InsertUser): Promise<User> {
    const id = randomUUID();
    const user: User = {
      ...insertUser,
      id,
      coins: insertUser.coins || 0,
      totalEarned: insertUser.totalEarned || 0,
      isBanned: insertUser.isBanned || false,
      banReason: insertUser.banReason || null,
      deviceToken: insertUser.deviceToken || null,
      referralCode: insertUser.referralCode || `REF${Date.now()}`,
      referredBy: insertUser.referredBy || null,
      createdAt: new Date(),
    };
    this.users.set(id, user);
    return user;
  }

  async updateUser(id: string, updates: Partial<User>): Promise<User | undefined> {
    const user = this.users.get(id);
    if (!user) return undefined;
    const updated = { ...user, ...updates };
    this.users.set(id, updated);
    return updated;
  }

  async banUser(id: string, reason?: string): Promise<void> {
    const user = this.users.get(id);
    if (user) {
      user.isBanned = true;
      user.banReason = reason || "Banned by admin";
      this.users.set(id, user);
    }
  }

  async unbanUser(id: string): Promise<void> {
    const user = this.users.get(id);
    if (user) {
      user.isBanned = false;
      user.banReason = null;
      this.users.set(id, user);
    }
  }

  // Transactions
  async getAllTransactions(): Promise<Transaction[]> {
    return Array.from(this.transactions.values()).sort((a, b) =>
      new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    );
  }

  async getTransaction(id: string): Promise<Transaction | undefined> {
    return this.transactions.get(id);
  }

  async createTransaction(insertTransaction: InsertTransaction): Promise<Transaction> {
    const id = randomUUID();
    const transaction: Transaction = {
      ...insertTransaction,
      id,
      status: insertTransaction.status || "completed",
      createdAt: new Date(),
    };
    this.transactions.set(id, transaction);
    return transaction;
  }

  // Offers
  async getAllOffers(): Promise<Offer[]> {
    return Array.from(this.offers.values()).sort((a, b) => b.priority - a.priority);
  }

  async getOffer(id: string): Promise<Offer | undefined> {
    return this.offers.get(id);
  }

  async createOffer(insertOffer: InsertOffer): Promise<Offer> {
    const id = randomUUID();
    const offer: Offer = {
      ...insertOffer,
      id,
      imageUrl: insertOffer.imageUrl || null,
      actionUrl: insertOffer.actionUrl || null,
      isActive: insertOffer.isActive ?? true,
      priority: insertOffer.priority || 0,
      createdAt: new Date(),
    };
    this.offers.set(id, offer);
    return offer;
  }

  async updateOffer(id: string, updates: Partial<Offer>): Promise<Offer | undefined> {
    const offer = this.offers.get(id);
    if (!offer) return undefined;
    const updated = { ...offer, ...updates };
    this.offers.set(id, updated);
    return updated;
  }

  async deleteOffer(id: string): Promise<void> {
    this.offers.delete(id);
  }

  async toggleOfferActive(id: string, isActive: boolean): Promise<void> {
    const offer = this.offers.get(id);
    if (offer) {
      offer.isActive = isActive;
      this.offers.set(id, offer);
    }
  }

  // Banners
  async getAllBanners(): Promise<Banner[]> {
    return Array.from(this.banners.values()).sort((a, b) => b.priority - a.priority);
  }

  async getBanner(id: string): Promise<Banner | undefined> {
    return this.banners.get(id);
  }

  async createBanner(insertBanner: InsertBanner): Promise<Banner> {
    const id = randomUUID();
    const banner: Banner = {
      ...insertBanner,
      id,
      linkUrl: insertBanner.linkUrl || null,
      isActive: insertBanner.isActive ?? true,
      priority: insertBanner.priority || 0,
      createdAt: new Date(),
    };
    this.banners.set(id, banner);
    return banner;
  }

  async updateBanner(id: string, updates: Partial<Banner>): Promise<Banner | undefined> {
    const banner = this.banners.get(id);
    if (!banner) return undefined;
    const updated = { ...banner, ...updates };
    this.banners.set(id, updated);
    return updated;
  }

  async deleteBanner(id: string): Promise<void> {
    this.banners.delete(id);
  }

  async toggleBannerActive(id: string, isActive: boolean): Promise<void> {
    const banner = this.banners.get(id);
    if (banner) {
      banner.isActive = isActive;
      this.banners.set(id, banner);
    }
  }

  // Promo Codes
  async getAllPromoCodes(): Promise<PromoCode[]> {
    return Array.from(this.promoCodes.values());
  }

  async getPromoCode(id: string): Promise<PromoCode | undefined> {
    return this.promoCodes.get(id);
  }

  async getPromoCodeByCode(code: string): Promise<PromoCode | undefined> {
    return Array.from(this.promoCodes.values()).find(pc => pc.code === code);
  }

  async createPromoCode(insertPromoCode: InsertPromoCode): Promise<PromoCode> {
    const id = randomUUID();
    const promoCode: PromoCode = {
      ...insertPromoCode,
      id,
      maxUses: insertPromoCode.maxUses || 0,
      usedCount: 0,
      expiresAt: insertPromoCode.expiresAt || null,
      isActive: insertPromoCode.isActive ?? true,
      createdAt: new Date(),
    };
    this.promoCodes.set(id, promoCode);
    return promoCode;
  }

  async deletePromoCode(id: string): Promise<void> {
    this.promoCodes.delete(id);
  }

  async togglePromoCodeActive(id: string, isActive: boolean): Promise<void> {
    const promoCode = this.promoCodes.get(id);
    if (promoCode) {
      promoCode.isActive = isActive;
      this.promoCodes.set(id, promoCode);
    }
  }

  // Support Tickets
  async getAllSupportTickets(): Promise<SupportTicket[]> {
    return Array.from(this.supportTickets.values()).sort((a, b) =>
      new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime()
    );
  }

  async getSupportTicket(id: string): Promise<SupportTicket | undefined> {
    return this.supportTickets.get(id);
  }

  async createSupportTicket(insertTicket: InsertSupportTicket): Promise<SupportTicket> {
    const id = randomUUID();
    const ticket: SupportTicket = {
      ...insertTicket,
      id,
      status: insertTicket.status || "open",
      priority: insertTicket.priority || "medium",
      createdAt: new Date(),
      updatedAt: new Date(),
    };
    this.supportTickets.set(id, ticket);
    return ticket;
  }

  async updateTicketStatus(id: string, status: string): Promise<void> {
    const ticket = this.supportTickets.get(id);
    if (ticket) {
      ticket.status = status;
      ticket.updatedAt = new Date();
      this.supportTickets.set(id, ticket);
    }
  }

  async getTicketMessages(ticketId: string): Promise<TicketMessage[]> {
    return Array.from(this.ticketMessages.values())
      .filter(msg => msg.ticketId === ticketId)
      .sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime());
  }

  async createTicketMessage(insertMessage: InsertTicketMessage): Promise<TicketMessage> {
    const id = randomUUID();
    const message: TicketMessage = {
      ...insertMessage,
      id,
      createdAt: new Date(),
    };
    this.ticketMessages.set(id, message);

    // Update ticket's updatedAt
    const ticket = this.supportTickets.get(insertMessage.ticketId);
    if (ticket) {
      ticket.updatedAt = new Date();
      this.supportTickets.set(insertMessage.ticketId, ticket);
    }

    return message;
  }

  // Achievements
  async getAllAchievements(): Promise<Achievement[]> {
    return Array.from(this.achievements.values());
  }

  async getAchievement(id: string): Promise<Achievement | undefined> {
    return this.achievements.get(id);
  }

  async createAchievement(insertAchievement: InsertAchievement): Promise<Achievement> {
    const id = randomUUID();
    const achievement: Achievement = {
      ...insertAchievement,
      id,
      isActive: insertAchievement.isActive ?? true,
      createdAt: new Date(),
    };
    this.achievements.set(id, achievement);
    return achievement;
  }

  async updateAchievement(id: string, updates: Partial<Achievement>): Promise<Achievement | undefined> {
    const achievement = this.achievements.get(id);
    if (!achievement) return undefined;
    const updated = { ...achievement, ...updates };
    this.achievements.set(id, updated);
    return updated;
  }

  async deleteAchievement(id: string): Promise<void> {
    this.achievements.delete(id);
  }

  async toggleAchievementActive(id: string, isActive: boolean): Promise<void> {
    const achievement = this.achievements.get(id);
    if (achievement) {
      achievement.isActive = isActive;
      this.achievements.set(id, achievement);
    }
  }

  // Tasks
  async getAllTasks(): Promise<Task[]> {
    return Array.from(this.tasks.values()).sort((a, b) => b.priority - a.priority);
  }

  async getTask(id: string): Promise<Task | undefined> {
    return this.tasks.get(id);
  }

  async createTask(insertTask: InsertTask): Promise<Task> {
    const id = randomUUID();
    const task: Task = {
      ...insertTask,
      id,
      actionUrl: insertTask.actionUrl || null,
      isActive: insertTask.isActive ?? true,
      priority: insertTask.priority || 0,
      createdAt: new Date(),
    };
    this.tasks.set(id, task);
    return task;
  }

  async updateTask(id: string, updates: Partial<Task>): Promise<Task | undefined> {
    const task = this.tasks.get(id);
    if (!task) return undefined;
    const updated = { ...task, ...updates };
    this.tasks.set(id, updated);
    return updated;
  }

  async deleteTask(id: string): Promise<void> {
    this.tasks.delete(id);
  }

  async toggleTaskActive(id: string, isActive: boolean): Promise<void> {
    const task = this.tasks.get(id);
    if (task) {
      task.isActive = isActive;
      this.tasks.set(id, task);
    }
  }

  // Withdrawals
  async getAllWithdrawals(): Promise<Withdrawal[]> {
    return Array.from(this.withdrawals.values()).sort((a, b) =>
      new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    );
  }

  async getWithdrawal(id: string): Promise<Withdrawal | undefined> {
    return this.withdrawals.get(id);
  }

  async createWithdrawal(insertWithdrawal: InsertWithdrawal): Promise<Withdrawal> {
    const id = randomUUID();
    const withdrawal: Withdrawal = {
      ...insertWithdrawal,
      id,
      status: "pending",
      adminNote: null,
      createdAt: new Date(),
      processedAt: null,
    };
    this.withdrawals.set(id, withdrawal);
    return withdrawal;
  }

  async updateWithdrawalStatus(id: string, status: string, adminNote?: string): Promise<void> {
    const withdrawal = this.withdrawals.get(id);
    if (withdrawal) {
      withdrawal.status = status;
      withdrawal.adminNote = adminNote || withdrawal.adminNote;
      withdrawal.processedAt = new Date();
      this.withdrawals.set(id, withdrawal);
    }
  }

  // Referrals
  async getAllReferrals(): Promise<Referral[]> {
    return Array.from(this.referrals.values());
  }

  async getReferral(id: string): Promise<Referral | undefined> {
    return this.referrals.get(id);
  }

  async createReferral(insertReferral: InsertReferral): Promise<Referral> {
    const id = randomUUID();
    const referral: Referral = {
      ...insertReferral,
      id,
      coinsEarned: insertReferral.coinsEarned || 0,
      status: insertReferral.status || "active",
      createdAt: new Date(),
    };
    this.referrals.set(id, referral);
    return referral;
  }

  // Leaderboard
  async getAllLeaderboard(): Promise<Leaderboard[]> {
    return Array.from(this.leaderboard.values()).sort((a, b) => a.rank - b.rank);
  }

  async updateLeaderboard(userId: string, totalCoins: number): Promise<void> {
    let entry = this.leaderboard.get(userId);
    if (!entry) {
      const id = randomUUID();
      entry = {
        id,
        userId,
        totalCoins,
        rank: 0,
        updatedAt: new Date(),
      };
    } else {
      entry.totalCoins = totalCoins;
      entry.updatedAt = new Date();
    }
    this.leaderboard.set(userId, entry);

    // Recalculate ranks
    const sorted = Array.from(this.leaderboard.values()).sort((a, b) => b.totalCoins - a.totalCoins);
    sorted.forEach((entry, idx) => {
      entry.rank = idx + 1;
      this.leaderboard.set(entry.userId, entry);
    });
  }

  // Notifications
  async getAllNotifications(): Promise<Notification[]> {
    return Array.from(this.notifications.values()).sort((a, b) =>
      new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    );
  }

  async getNotification(id: string): Promise<Notification | undefined> {
    return this.notifications.get(id);
  }

  async createNotification(insertNotification: InsertNotification): Promise<Notification> {
    const id = randomUUID();
    const notification: Notification = {
      ...insertNotification,
      id,
      targetUsers: insertNotification.targetUsers || null,
      segment: insertNotification.segment || null,
      status: insertNotification.status || "draft",
      sentAt: insertNotification.status === "sent" ? new Date() : null,
      createdAt: new Date(),
    };
    this.notifications.set(id, notification);
    return notification;
  }

  // Auto-Ban Rules
  async getAllAutoBanRules(): Promise<AutoBanRule[]> {
    return Array.from(this.autoBanRules.values());
  }

  async getAutoBanRule(id: string): Promise<AutoBanRule | undefined> {
    return this.autoBanRules.get(id);
  }

  async createAutoBanRule(insertRule: InsertAutoBanRule): Promise<AutoBanRule> {
    const id = randomUUID();
    const rule: AutoBanRule = {
      ...insertRule,
      id,
      isActive: insertRule.isActive ?? true,
      createdAt: new Date(),
    };
    this.autoBanRules.set(id, rule);
    return rule;
  }

  async deleteAutoBanRule(id: string): Promise<void> {
    this.autoBanRules.delete(id);
  }

  async toggleAutoBanRuleActive(id: string, isActive: boolean): Promise<void> {
    const rule = this.autoBanRules.get(id);
    if (rule) {
      rule.isActive = isActive;
      this.autoBanRules.set(id, rule);
    }
  }
}

export const storage = new MemStorage();
