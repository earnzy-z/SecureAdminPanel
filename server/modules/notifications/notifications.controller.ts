import type { Express, Request, Response } from "express";
import { storage } from "../../storage";
import { insertNotificationSchema } from "@shared/schema";

export function setupNotificationsRoutes(app: Express) {
  app.get("/api/notifications", async (req: Request, res: Response) => {
    try {
      const notifications = await storage.getAllNotifications();
      res.json(notifications);
    } catch (error: any) {
      res.status(500).json({ error: error.message });
    }
  });

  app.post("/api/notifications", async (req: Request, res: Response) => {
    try {
      const data = insertNotificationSchema.parse(req.body);
      const notification = await storage.createNotification(data);
      res.json(notification);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });
}
