import type { Request, Response } from "express";
import { DynamoService } from "../services/dynamo.service";
import { S3Service } from "../services/s3.service";
import { v4 as uuidv4 } from "uuid";

export class OffersController {
  static async getOffers(req: Request, res: Response) {
    try {
      const offers = await DynamoService.queryByType("OFFER");
      res.json(offers);
    } catch (error: any) {
      res.status(500).json({ error: error.message });
    }
  }

  static async createOffer(req: Request, res: Response) {
    try {
      const { title, description, reward, category, isActive } = req.body;
      const offerId = uuidv4();

      const offer = await DynamoService.createItem("OFFER", offerId, {
        title,
        description,
        reward,
        category,
        isActive: isActive || true,
      });

      res.json(offer);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  }

  static async updateOffer(req: Request, res: Response) {
    try {
      const offers = await DynamoService.queryByType("OFFER");
      const offer = offers.find((o: any) => o.ID === req.params.id);

      if (!offer) return res.status(404).json({ error: "Offer not found" });

      const updated = await DynamoService.updateItem(
        offer.PK,
        offer.SK,
        req.body
      );

      res.json(updated);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  }

  static async deleteOffer(req: Request, res: Response) {
    try {
      const offers = await DynamoService.queryByType("OFFER");
      const offer = offers.find((o: any) => o.ID === req.params.id);

      if (!offer) return res.status(404).json({ error: "Offer not found" });

      await DynamoService.deleteItem(offer.PK, offer.SK);
      res.json({ success: true });
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  }

  static async getUploadUrl(req: Request, res: Response) {
    try {
      const { filename, contentType } = req.body;
      const key = `offers/${uuidv4()}/${filename}`;
      const uploadUrl = await S3Service.getUploadUrl(key, contentType);
      const fileUrl = S3Service.getFileUrl(key);

      res.json({ uploadUrl, fileUrl, key });
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  }
}
