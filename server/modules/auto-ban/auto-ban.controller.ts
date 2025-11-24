import type { Express, Request, Response } from "express";
import { storage } from "../../storage";
import { insertAutoBanRuleSchema } from "@shared/schema";

export function setupAutoBanRoutes(app: Express) {
  app.get("/api/auto-ban-rules", async (req: Request, res: Response) => {
    try {
      const rules = await storage.getAllAutoBanRules();
      res.json(rules);
    } catch (error: any) {
      res.status(500).json({ error: error.message });
    }
  });

  app.post("/api/auto-ban-rules", async (req: Request, res: Response) => {
    try {
      const data = insertAutoBanRuleSchema.parse(req.body);
      const rule = await storage.createAutoBanRule(data);
      res.json(rule);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });

  app.delete("/api/auto-ban-rules/:id", async (req: Request, res: Response) => {
    try {
      await storage.deleteAutoBanRule(req.params.id);
      res.json({ success: true });
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });

  app.post("/api/auto-ban-rules/:id/toggle", async (req: Request, res: Response) => {
    try {
      const { isActive } = req.body;
      await storage.toggleAutoBanRuleActive(req.params.id, isActive);
      res.json({ success: true });
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });
}
