import type { RequestHandler } from "express";

// Simple admin verification - in production, check user roles from database
const ADMIN_IDS = new Set(process.env.ADMIN_IDS?.split(",") || []);

export const isAuthenticated: RequestHandler = async (req, res, next) => {
  const user = (req as any).user as any;

  if (!req.isAuthenticated() || !user) {
    return res.status(401).json({ message: "Unauthorized" });
  }

  // Optionally verify admin status
  if (ADMIN_IDS.size > 0 && !ADMIN_IDS.has(user.claims?.sub)) {
    return res.status(403).json({ message: "Forbidden" });
  }

  return next();
};
