import type { Express, Request, Response } from "express";
import { storage } from "../../storage";
import { insertBannerSchema } from "@shared/schema";

export function setupBannersRoutes(app: Express) {
  app.get("/api/banners", async (req: Request, res: Response) => {
    try {
      const banners = await storage.getAllBanners();
      res.json(banners);
    } catch (error: any) {
      res.status(500).json({ error: error.message });
    }
  });

  app.post("/api/banners", async (req: Request, res: Response) => {
    try {
      const data = insertBannerSchema.parse(req.body);
      const banner = await storage.createBanner(data);
      res.json(banner);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });

  app.patch("/api/banners/:id", async (req: Request, res: Response) => {
    try {
      const banner = await storage.updateBanner(req.params.id, req.body);
      res.json(banner);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });

  app.delete("/api/banners/:id", async (req: Request, res: Response) => {
    try {
      await storage.deleteBanner(req.params.id);
      res.json({ success: true });
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });

  app.post("/api/banners/:id/toggle", async (req: Request, res: Response) => {
    try {
      const { isActive } = req.body;
      await storage.toggleBannerActive(req.params.id, isActive);
      res.json({ success: true });
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });
}
