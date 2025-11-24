import type { Express, Request, Response } from "express";
import { storage } from "../../storage";

export function setupSupportRoutes(app: Express) {
  app.get("/api/support/tickets", async (req: Request, res: Response) => {
    try {
      const tickets = await storage.getAllSupportTickets();
      res.json(tickets);
    } catch (error: any) {
      res.status(500).json({ error: error.message });
    }
  });

  app.get("/api/support/tickets/:id", async (req: Request, res: Response) => {
    try {
      const ticket = await storage.getSupportTicket(req.params.id);
      if (!ticket) return res.status(404).json({ error: "Ticket not found" });
      res.json(ticket);
    } catch (error: any) {
      res.status(500).json({ error: error.message });
    }
  });

  app.get("/api/support/tickets/:id/messages", async (req: Request, res: Response) => {
    try {
      const messages = await storage.getTicketMessages(req.params.id);
      res.json(messages);
    } catch (error: any) {
      res.status(500).json({ error: error.message });
    }
  });

  app.post("/api/support/tickets/:id/messages", async (req: Request, res: Response) => {
    try {
      const message = await storage.createTicketMessage({
        ticketId: req.params.id,
        senderId: req.body.senderId,
        senderType: "admin",
        message: req.body.message,
      });
      res.json(message);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });

  app.post("/api/support/tickets/:id/status", async (req: Request, res: Response) => {
    try {
      const { status } = req.body;
      await storage.updateTicketStatus(req.params.id, status);
      res.json({ success: true });
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });
}
