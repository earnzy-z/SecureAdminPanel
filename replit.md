# Earnzy Admin Panel - Professional Edition

## Overview

**Earnzy Admin Panel** is a comprehensive, secured admin dashboard for managing an earning app platform. Built with a professional microservices architecture, it supports 50+ advanced features including user management, real-time analytics, FCM push notifications, AWS DynamoDB single-table design, image uploads, live chat support, withdrawal management, and more.

**Tech Stack:**
- **Backend:** Express.js with AWS Lambda/API Gateway
- **Database:** AWS DynamoDB (single-table design)
- **Storage:** AWS S3 for image uploads
- **Notifications:** Firebase Cloud Messaging (FCM)
- **Real-time Chat:** WebSocket server with Socket.IO
- **Frontend:** React + Vite with Tailwind CSS + shadcn/ui
- **Infrastructure:** Terraform / CloudFormation for AWS

## Project Structure

```
earnzy-admin/
├── services/api/              # Main API service (Node/Express)
├── services/chat/             # Real-time WebSocket chat server
├── services/worker/           # Background jobs (notifications, batch)
├── web/                       # React admin dashboard
├── infra/                     # AWS infrastructure (Terraform/CloudFormation)
├── scripts/                   # Deployment and utility scripts
└── docs/                      # Architecture and security documentation
```

## Core Features (50+)

### User Management (8 features)
- ✅ User list & search
- ✅ User profile detail view
- ✅ Transaction history per user
- ✅ Manual ban/unban
- ✅ Auto-ban rules configuration
- ✅ User coin adjustment
- ✅ Referral tracking
- ✅ Leaderboard management

### Financial Management (8 features)
- ✅ Coin control & adjustment
- ✅ Transaction tracking & filtering
- ✅ Withdrawal approval/rejection
- ✅ Withdrawal limits configuration
- ✅ Bonus & reward allocation
- ✅ Promo code management
- ✅ Real-time balance analytics
- ✅ Transaction reports & exports

### Content Management (7 features)
- ✅ Offer management (add/edit/remove/toggle)
- ✅ Banner management with image upload
- ✅ Offerwall controller
- ✅ Cards controller (offer cards)
- ✅ Task management
- ✅ Achievement management
- ✅ App UI control for Android

### Communications (5 features)
- ✅ Push notification (FCM) sending
- ✅ Notification templates
- ✅ Support ticket management
- ✅ Live chat with admin-user
- ✅ Bulk notification campaigns

### Mobile App Control (6 features)
- ✅ Homepage config controller
- ✅ Theme controller
- ✅ Android app version control
- ✅ Feature flags
- ✅ In-app message delivery
- ✅ Maintenance mode control

### Analytics & Monitoring (8 features)
- ✅ Real-time dashboard stats
- ✅ User engagement metrics
- ✅ Revenue analytics
- ✅ Admin activity logs
- ✅ System health monitoring
- ✅ API performance tracking
- ✅ Error tracking & alerts
- ✅ Export reports (CSV/PDF)

### Security & Admin (8+ features)
- ✅ JWT-based authentication
- ✅ Role-based access control (RBAC)
- ✅ Rate limiting
- ✅ Audit logging
- ✅ Admin permission management
- ✅ Secure image uploads (S3 signed URLs)
- ✅ Encrypted sensitive data
- ✅ IP whitelist configuration

## Database Design

### DynamoDB Single-Table Design

**Table Name:** `earnzy-admin`

**Primary Key:**
- `PK` (Partition Key): `TYPE#ID` (e.g., `USER#abc123`, `TRANSACTION#xyz789`)
- `SK` (Sort Key): `CREATED_AT#ID` (for efficient time-range queries)

**Global Secondary Indexes:**
1. `GSI1-Index`: Email lookups, phone lookups
2. `GSI2-Index`: User referral chains
3. `GSI3-Index`: Transaction filtering by type/status

**Data Entities:**
- Users (with ban status, coins, referral info)
- Transactions (earn, spend, bonus, referral, withdrawal)
- Offers (with categories, rewards)
- Banners (with image URLs, priority)
- Tasks (with completion tracking)
- Achievements (with user progress)
- Promo Codes (with usage limits)
- Support Tickets (with messages)
- Withdrawals (with approval workflow)
- Notifications (with delivery status)
- Auto-Ban Rules (with trigger conditions)
- Admin Audit Logs

## Authentication & Security

**Authentication Flow:**
1. Admin login via email/password or OAuth (Google, GitHub)
2. JWT token generation (exp: 24 hours)
3. Refresh token rotation
4. Session stored in DynamoDB

**Authorization:**
- Role-based access control (ADMIN, MANAGER, MODERATOR)
- Permission-based endpoint access
- Resource-level authorization (admin can't edit other admins)

**Security Measures:**
- HTTPS enforced
- CORS configured
- Rate limiting (100 req/min per IP)
- Input validation & sanitization
- OWASP security headers
- Audit logging for all admin actions
- Encrypted passwords (bcrypt)
- Encrypted sensitive fields in DB

## API Endpoints (40+)

### Auth Routes
- `POST /api/auth/login` - Admin login
- `POST /api/auth/logout` - Logout
- `GET /api/auth/user` - Current admin info
- `POST /api/auth/refresh` - Refresh token

### Users Routes
- `GET /api/users` - List all users
- `GET /api/users/:id` - User details
- `POST /api/users/:id/ban` - Ban user
- `POST /api/users/:id/unban` - Unban user
- `GET /api/users/:id/transactions` - User transactions

### Coins Routes
- `POST /api/coins/adjust` - Add/deduct coins
- `POST /api/coins/bulk-credit` - Bulk credit coins
- `GET /api/coins/history/:userId` - Coin history

### Transactions Routes
- `GET /api/transactions` - List transactions
- `GET /api/transactions/:id` - Transaction details

### Offers Routes
- `GET /api/offers` - List offers
- `POST /api/offers` - Create offer
- `PATCH /api/offers/:id` - Update offer
- `DELETE /api/offers/:id` - Delete offer
- `POST /api/offers/:id/toggle` - Toggle active status

### Withdrawals Routes
- `GET /api/withdrawals` - List withdrawals
- `POST /api/withdrawals/:id/approve` - Approve withdrawal
- `POST /api/withdrawals/:id/reject` - Reject withdrawal

### Support Routes
- `GET /api/support/tickets` - List tickets
- `GET /api/support/tickets/:id` - Ticket details
- `GET /api/support/tickets/:id/messages` - Ticket messages
- `POST /api/support/tickets/:id/messages` - Send message
- `POST /api/support/tickets/:id/status` - Update status

### Notifications Routes
- `GET /api/notifications` - List notifications
- `POST /api/notifications` - Send notification (FCM)
- `POST /api/notifications/bulk` - Bulk send

### Other Routes
- `GET /api/stats` - Dashboard statistics
- `GET /api/leaderboard` - User leaderboard
- `GET /api/referrals` - Referral data
- `GET /api/auto-ban-rules` - Auto-ban rules
- `GET /api/promo-codes` - Promo codes
- `GET /api/admin/logs` - Activity logs

## Frontend Pages (20+)

1. **Dashboard** - Real-time stats & charts
2. **Users** - List, search, view details, ban/unban
3. **Transactions** - Filter, search, export
4. **Coins** - Adjust, bulk credit, history
5. **Offers** - CRUD operations
6. **Banners** - Upload, manage, reorder
7. **Tasks** - Create, edit, manage
8. **Achievements** - Configure progression
9. **Notifications** - Send FCM, templates
10. **Promo Codes** - Generate, manage
11. **Support Tickets** - View, respond, live chat
12. **Leaderboard** - View rankings
13. **Referrals** - Referral analytics
14. **Withdrawals** - Approve/reject requests
15. **Auto-Ban Rules** - Configure rules
16. **App UI Control** - Android UI settings
17. **Admin Logs** - Activity audit trail
18. **Settings** - Profile, theme, security
19. **Analytics** - Reports & exports
20. **Live Chat** - Real-time support interface

## Environment Variables

```env
# AWS
AWS_REGION=ap-south-1
AWS_ACCESS_KEY_ID=
AWS_SECRET_ACCESS_KEY=

# DynamoDB
DYNAMODB_TABLE=earnzy-admin
DYNAMODB_REGION=ap-south-1

# S3
S3_BUCKET=earnzy-uploads
S3_REGION=ap-south-1

# FCM
FCM_PROJECT_ID=
FCM_PRIVATE_KEY=

# Auth
JWT_SECRET=
REFRESH_TOKEN_SECRET=

# API
API_PORT=3000
NODE_ENV=development

# URLs
FRONTEND_URL=http://localhost:3000
ADMIN_URL=http://localhost:3001
```

## Deployment

**AWS Infrastructure:**
- Lambda functions for serverless API
- API Gateway for routing
- DynamoDB for data
- S3 for uploads
- CloudWatch for logging
- SNS/SQS for async jobs

**Deployment Steps:**
```bash
# Local development
npm run dev

# Build
npm run build

# Deploy to AWS
npm run deploy:dev      # Development
npm run deploy:prod     # Production
```

## Mobile Responsive Design

The admin panel is fully mobile-responsive using Tailwind CSS with breakpoints:
- Mobile: 320px - 640px
- Tablet: 641px - 1024px
- Desktop: 1025px+

All pages adapt automatically with optimized touch interactions.

## User Preferences

Preferred communication style: Simple, everyday language focused on functionality and results.

## Next Steps

1. Set up AWS credentials in environment
2. Deploy DynamoDB table using Terraform
3. Configure FCM credentials
4. Deploy API to Lambda
5. Deploy frontend to Vercel/S3 + CloudFront
6. Configure custom domain & SSL
7. Set up monitoring & alerts

## Documentation

- `/docs/architecture.md` - Detailed architecture decisions
- `/docs/security.md` - Security implementation guide
- `/docs/dynamodb-single-table-design.md` - Database design patterns
