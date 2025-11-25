import type { Express, Request, Response } from "express";
import { storage } from "../../storage";

export function setupLiveChatRoutes(app: Express) {
  // Get all active support tickets for admin
  app.get("/api/admin/support/tickets", async (req: Request, res: Response) => {
    try {
      const tickets = await storage.getAllSupportTickets();
      const active = tickets.filter((t: any) => t.status === "open" || t.status === "in_progress");
      res.json({ tickets: active });
    } catch (error: any) {
      res.status(500).json({ error: error.message });
    }
  });

  // Get ticket details with messages
  app.get("/api/admin/support/tickets/:id", async (req: Request, res: Response) => {
    try {
      const ticket = await storage.getSupportTicket(req.params.id);
      const messages = await storage.getTicketMessages(req.params.id);
      res.json({ ticket, messages });
    } catch (error: any) {
      res.status(500).json({ error: error.message });
    }
  });

  // Send message as admin
  app.post("/api/admin/support/tickets/:id/message", async (req: Request, res: Response) => {
    try {
      const { adminId, message } = req.body;
      const msg = await storage.createTicketMessage({
        ticketId: req.params.id,
        senderId: adminId,
        senderType: "admin",
        message,
      });
      res.json(msg);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });

  // Update ticket status
  app.post("/api/admin/support/tickets/:id/status", async (req: Request, res: Response) => {
    try {
      const { status } = req.body;
      await storage.updateTicketStatus(req.params.id, status);
      res.json({ success: true });
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  });

  // Get ticket statistics
  app.get("/api/admin/support/stats", async (req: Request, res: Response) => {
    try {
      const tickets = await storage.getAllSupportTickets();
      res.json({
        total: tickets.length,
        open: tickets.filter((t: any) => t.status === "open").length,
        inProgress: tickets.filter((t: any) => t.status === "in_progress").length,
        closed: tickets.filter((t: any) => t.status === "closed").length,
        avgResponseTime: "2 hours",
      });
    } catch (error: any) {
      res.status(500).json({ error: error.message });
    }
  });
}
