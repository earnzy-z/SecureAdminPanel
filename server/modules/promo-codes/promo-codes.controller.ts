import type { Express, Request, Response } from "express";
import { storage } from "../../storage";
import { insertPromoCodeSchema } from "@shared/schema";

export function setupPromoCodesRoutes(app: Express) {
  app.get("/api/promo-codes", async (req: Request, res: Response) => {
    try {
      const promoCodes = await storage.getAllPromoCodes();
      res.json(promoCodes);
    } catch (error: any) {
      res.status(500).json({ error: error.message });
    }
  });

  app.post("/api/promo-codes", async (req: Request, res: Response) => {
    try {
      const data = insertPromoCodeSchema.parse(req.body);
      const promoCode = await storage.createPromoCode(data);
      res.json(promoCode);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });

  app.delete("/api/promo-codes/:id", async (req: Request, res: Response) => {
    try {
      await storage.deletePromoCode(req.params.id);
      res.json({ success: true });
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });

  app.post("/api/promo-codes/:id/toggle", async (req: Request, res: Response) => {
    try {
      const { isActive } = req.body;
      await storage.togglePromoCodeActive(req.params.id, isActive);
      res.json({ success: true });
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });
}
