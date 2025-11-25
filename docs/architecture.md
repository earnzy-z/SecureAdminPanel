# Earnzy Admin Panel - Architecture Guide

## Overview

The Earnzy Admin Panel is built using a modular, microservices-based architecture designed for scalability, security, and maintainability. This document outlines the key architectural decisions and patterns used throughout the project.

## System Architecture

```
┌─────────────────┐
│   React Admin   │
│   Dashboard     │
│  (Vite + SPA)   │
└────────┬────────┘
         │
    ┌────▼────┐
    │ API GW  │
    │ (CORS)  │
    └────┬────┘
         │
    ┌────▼──────────────────┐
    │  Express API Server    │
    │  ├─ Auth Routes       │
    │  ├─ Users Routes      │
    │  ├─ Offers Routes     │
    │  ├─ Banners Routes    │
    │  └─ ... (40+ routes)  │
    └────┬──────────────────┘
         │
    ┌────▼──────────────┐
    │  DynamoDB         │
    │  (Single Table)   │
    └────┬──────────────┘
         │
    ┌────▼───────┬─────────┬──────────┐
    │   S3       │   FCM   │ CloudWatch
    │ (Uploads)  │ (Push)  │ (Logs)
    └────────────┴─────────┴──────────┘
```

## DynamoDB Single-Table Design

### Key Strategy
- **Single Table**: `earnzy-admin`
- **Composite Keys**: `PK` (Type#ID) + `SK` (Timestamp#ID)
- **Global Secondary Indexes**: For cross-cutting queries (email lookup, referral chains)
- **Item Types**: USER, TRANSACTION, OFFER, BANNER, TASK, ACHIEVEMENT, etc.

### Key Patterns

#### 1. Item Structure
```json
{
  "PK": "USER#user-001",
  "SK": "2025-01-20T10:30:00Z#user-001",
  "TYPE": "USER",
  "ID": "user-001",
  "CREATED_AT": "2025-01-20T10:30:00Z",
  "UPDATED_AT": "2025-01-20T10:30:00Z",
  "email": "user@example.com",
  "coins": 1000,
  "isBanned": false
}
```

#### 2. Query Patterns
- Get all users: `Query PK = USER#*`
- Get user by ID: `Get PK = USER#user-001`
- Time-range queries: `Query PK = USER#* AND SK BETWEEN date1 AND date2`
- GSI lookup by email: `Query GSI1PK = EMAIL#user@example.com`

### Global Secondary Indexes

| Index | PK | SK | Use Case |
|-------|----|----|----------|
| GSI1 | EMAIL#email | USER#id | Email lookup for login |
| GSI2 | REFERRER_ID#id | CREATED_AT#id | Referral chain queries |
| GSI3 | TYPE#status | CREATED_AT#id | Status-based filtering |

## Backend Architecture

### Modular Structure

```
services/api/src/
├── routes/          # Route handlers
├── controllers/     # Business logic
├── services/        # AWS integrations (DynamoDB, S3, FCM)
├── middlewares/     # Auth, RBAC, rate limiting
├── models/          # Type definitions
├── utils/           # Helpers (logger, validators)
└── config/          # Configuration
```

### Route Organization
Routes are organized by feature, not by HTTP method:
- `/routes/auth.routes.ts` - Authentication flows
- `/routes/users.routes.ts` - User management
- `/routes/offers.routes.ts` - Offer management
- `/routes/withdrawals.routes.ts` - Withdrawal handling
- etc.

### Controller Pattern
Controllers handle HTTP request/response, validation, and call services:

```typescript
export class UsersController {
  static async getUser(req, res) {
    const user = await DynamoService.getItem(`USER#${req.params.id}`);
    res.json(user);
  }
}
```

### Service Pattern
Services encapsulate business logic and external integrations:

```typescript
export class DynamoService {
  static async createItem(itemType, itemId, data) {
    // DynamoDB single-table logic
  }
}
```

## Authentication & Authorization

### JWT Flow
1. Admin logs in with email/password
2. Server generates JWT with admin ID and role
3. JWT sent to frontend, stored in secure cookie/localStorage
4. Subsequent requests include JWT in Authorization header
5. Middleware validates JWT and attaches admin context to request

### Role-Based Access Control (RBAC)
Three admin roles with different permissions:
- **SUPER_ADMIN**: All permissions
- **ADMIN**: Most features except financial controls
- **MODERATOR**: User management and support only

## Real-Time Features

### WebSocket Chat Server
- Socket.IO server for real-time support tickets
- Connection authentication with JWT
- Room-based messaging (per ticket)
- Message persistence in DynamoDB

### FCM Notifications
- Firebase Cloud Messaging for push notifications
- Topic-based subscriptions for bulk sends
- Device token management in DynamoDB
- Delivery status tracking

## Security Measures

### 1. Input Validation
- Zod schemas for all request bodies
- Email/phone validation
- Amount validation for financial operations

### 2. Rate Limiting
- 100 requests per minute per IP
- Per-endpoint rate limits for sensitive operations

### 3. Audit Logging
- All admin actions logged with timestamp, admin ID, action type
- Sensitive operations (coin adjustments, bans) logged
- Logs stored in DynamoDB for audit trail

### 4. Data Encryption
- Passwords hashed with bcrypt
- Sensitive fields encrypted before storage
- S3 signed URLs for file uploads (time-limited)

### 5. OWASP Headers
- Content-Security-Policy
- X-Frame-Options: DENY
- X-Content-Type-Options: nosniff
- Strict-Transport-Security (HTTPS only)

## Frontend Architecture

### Page Organization
Pages grouped by feature:
- `/pages/users/` - User management
- `/pages/transactions/` - Transaction history
- `/pages/offers/` - Offer CRUD
- `/pages/support/` - Support tickets & live chat
- etc.

### State Management
- React Query for server state
- Context API for auth state
- LocalStorage for theme preference

### Component Hierarchy
```
App
├── AuthProvider
├── Sidebar (Navigation)
├── MainLayout
│   ├── Header
│   └── Router
│       ├── Dashboard
│       ├── UsersPage
│       ├── OffersPage
│       └── ...
```

## Deployment Strategy

### Local Development
```bash
npm run dev:api      # Start Express server
npm run dev:web      # Start Vite + React
```

### AWS Lambda Deployment
- API service deployed as Lambda behind API Gateway
- Cold start optimization (keep-alive, connection pooling)
- Environment variables via AWS Secrets Manager

### Frontend Deployment
- Built with Vite for production-ready bundle
- Deployed to S3 + CloudFront for global CDN
- API calls proxied through API Gateway

### Database
- DynamoDB on-demand pricing for variable workloads
- Point-in-time recovery enabled
- Automated backups

## Monitoring & Logging

### CloudWatch Integration
- Request/response logging
- Lambda execution metrics
- Error tracking and alerts
- Custom metrics for business events

### Structured Logging
All logs in JSON format for easy parsing:
```json
{
  "timestamp": "2025-01-20T10:30:00Z",
  "level": "INFO",
  "message": "User banned",
  "adminId": "admin-001",
  "userId": "user-123"
}
```

## Performance Considerations

### 1. Caching
- Query results cached in memory for 1 minute
- S3 signed URLs cached for 1 hour
- Frontend React Query cache for user/offer data

### 2. Pagination
- All list endpoints support `limit` and `exclusiveStartKey`
- Default limit 100 items
- DynamoDB native pagination support

### 3. Database Indexes
- Primary key queries: O(1)
- GSI queries: O(n) but with partition pruning
- Scan operations avoided in production

## API Response Format

All responses follow a consistent format:

### Success Response
```json
{
  "data": { /* resource data */ },
  "meta": { /* pagination, timestamps */ }
}
```

### Error Response
```json
{
  "error": "Error message",
  "code": "ERROR_CODE",
  "meta": { /* context */ }
}
```

## Future Improvements

1. **Caching Layer**: Add Redis for session caching
2. **Message Queue**: SQS for async notifications
3. **Analytics**: Kinesis for real-time analytics
4. **API Versioning**: v1/, v2/ routes for backward compatibility
5. **GraphQL**: Optional GraphQL layer for complex queries
