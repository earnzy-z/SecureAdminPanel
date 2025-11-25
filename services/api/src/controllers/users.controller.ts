import type { Request, Response } from "express";
import { DynamoService } from "../services/dynamo.service";
import { v4 as uuidv4 } from "uuid";

export class UsersController {
  static async getAllUsers(req: Request, res: Response) {
    try {
      const users = await DynamoService.queryByType("USER");
      res.json(users);
    } catch (error: any) {
      res.status(500).json({ error: error.message });
    }
  }

  static async getUser(req: Request, res: Response) {
    try {
      const user = await DynamoService.getItem(`USER#${req.params.id}`);
      if (!user) return res.status(404).json({ error: "User not found" });
      res.json(user);
    } catch (error: any) {
      res.status(500).json({ error: error.message });
    }
  }

  static async banUser(req: Request, res: Response) {
    try {
      const { reason } = req.body;
      const users = await DynamoService.queryByType("USER");
      const user = users.find((u: any) => u.ID === req.params.id);
      
      if (!user) return res.status(404).json({ error: "User not found" });

      const pk = user.PK;
      const sk = user.SK;
      
      await DynamoService.updateItem(pk, sk, {
        isBanned: true,
        banReason: reason,
        bannedAt: new Date().toISOString(),
      });

      res.json({ success: true });
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  }

  static async unbanUser(req: Request, res: Response) {
    try {
      const users = await DynamoService.queryByType("USER");
      const user = users.find((u: any) => u.ID === req.params.id);
      
      if (!user) return res.status(404).json({ error: "User not found" });

      const pk = user.PK;
      const sk = user.SK;
      
      await DynamoService.updateItem(pk, sk, {
        isBanned: false,
        banReason: null,
        bannedAt: null,
      });

      res.json({ success: true });
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  }
}
