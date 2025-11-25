import type { Express } from "express";
import { OffersController } from "../controllers/offers.controller";

export function setupOffersRoutes(app: Express) {
  app.get("/api/offers", OffersController.getOffers);
  app.post("/api/offers", OffersController.createOffer);
  app.patch("/api/offers/:id", OffersController.updateOffer);
  app.delete("/api/offers/:id", OffersController.deleteOffer);
  app.post("/api/offers/upload-url", OffersController.getUploadUrl);
}
