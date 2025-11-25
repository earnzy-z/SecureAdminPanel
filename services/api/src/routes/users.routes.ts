import type { Express } from "express";
import { UsersController } from "../controllers/users.controller";

export function setupUsersRoutes(app: Express) {
  app.get("/api/users", UsersController.getAllUsers);
  app.get("/api/users/:id", UsersController.getUser);
  app.post("/api/users/:id/ban", UsersController.banUser);
  app.post("/api/users/:id/unban", UsersController.unbanUser);
}
