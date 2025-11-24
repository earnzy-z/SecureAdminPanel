import type { Express, Request, Response } from "express";
import { storage } from "../../storage";
import { insertOfferSchema } from "@shared/schema";

export function setupOffersRoutes(app: Express) {
  app.get("/api/offers", async (req: Request, res: Response) => {
    try {
      const offers = await storage.getAllOffers();
      res.json(offers);
    } catch (error: any) {
      res.status(500).json({ error: error.message });
    }
  });

  app.get("/api/offers/:id", async (req: Request, res: Response) => {
    try {
      const offer = await storage.getOffer(req.params.id);
      if (!offer) return res.status(404).json({ error: "Offer not found" });
      res.json(offer);
    } catch (error: any) {
      res.status(500).json({ error: error.message });
    }
  });

  app.post("/api/offers", async (req: Request, res: Response) => {
    try {
      const data = insertOfferSchema.parse(req.body);
      const offer = await storage.createOffer(data);
      res.json(offer);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });

  app.patch("/api/offers/:id", async (req: Request, res: Response) => {
    try {
      const offer = await storage.updateOffer(req.params.id, req.body);
      res.json(offer);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });

  app.delete("/api/offers/:id", async (req: Request, res: Response) => {
    try {
      await storage.deleteOffer(req.params.id);
      res.json({ success: true });
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });

  app.post("/api/offers/:id/toggle", async (req: Request, res: Response) => {
    try {
      const { isActive } = req.body;
      await storage.toggleOfferActive(req.params.id, isActive);
      res.json({ success: true });
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });
}
