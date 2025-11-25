import type { Request, Response } from "express";
import { DynamoService } from "../services/dynamo.service";
import { v4 as uuidv4 } from "uuid";

export class TransactionsController {
  static async getTransactions(req: Request, res: Response) {
    try {
      const transactions = await DynamoService.queryByType("TRANSACTION");
      res.json(transactions);
    } catch (error: any) {
      res.status(500).json({ error: error.message });
    }
  }

  static async getUserTransactions(req: Request, res: Response) {
    try {
      const transactions = await DynamoService.queryByType("TRANSACTION");
      const userTransactions = transactions.filter(
        (t: any) => t.userId === req.params.userId
      );
      res.json(userTransactions);
    } catch (error: any) {
      res.status(500).json({ error: error.message });
    }
  }

  static async createTransaction(req: Request, res: Response) {
    try {
      const { userId, type, amount, description } = req.body;
      const transactionId = uuidv4();

      const transaction = await DynamoService.createItem(
        "TRANSACTION",
        transactionId,
        {
          userId,
          type,
          amount,
          description,
          status: "completed",
        }
      );

      res.json(transaction);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  }
}
