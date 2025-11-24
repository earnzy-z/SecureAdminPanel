import type { Express } from "express";
import { setupAuthRoutes } from "./auth/auth.controller";
import { setupUsersRoutes } from "./users/users.controller";
import { setupCoinsRoutes } from "./coins/coins.controller";
import { setupTransactionsRoutes } from "./transactions/transactions.controller";
import { setupOffersRoutes } from "./offers/offers.controller";
import { setupBannersRoutes } from "./banners/banners.controller";
import { setupTasksRoutes } from "./tasks/tasks.controller";
import { setupAchievementsRoutes } from "./achievements/achievements.controller";
import { setupPromoCodesRoutes } from "./promo-codes/promo-codes.controller";
import { setupWithdrawalsRoutes } from "./withdrawals/withdrawals.controller";
import { setupSupportRoutes } from "./support/support.controller";
import { setupNotificationsRoutes } from "./notifications/notifications.controller";
import { setupLeaderboardRoutes } from "./leaderboard/leaderboard.controller";
import { setupReferralsRoutes } from "./referrals/referrals.controller";
import { setupAutoBanRoutes } from "./auto-ban/auto-ban.controller";

export function registerModules(app: Express) {
  setupAuthRoutes(app);
  setupUsersRoutes(app);
  setupCoinsRoutes(app);
  setupTransactionsRoutes(app);
  setupOffersRoutes(app);
  setupBannersRoutes(app);
  setupTasksRoutes(app);
  setupAchievementsRoutes(app);
  setupPromoCodesRoutes(app);
  setupWithdrawalsRoutes(app);
  setupSupportRoutes(app);
  setupNotificationsRoutes(app);
  setupLeaderboardRoutes(app);
  setupReferralsRoutes(app);
  setupAutoBanRoutes(app);
}
