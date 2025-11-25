# Earnzy Backend - Cloudflare Workers

A serverless backend for the Earnzy earning app, built with Cloudflare Workers, Hono, and Cloudflare KV storage.

## Features

✅ Authentication & Session Management
✅ Task Management & Completion Tracking
✅ Offer Claims & Offerwall
✅ Referral System with Deep Links
✅ Coin Management & Balance Tracking
✅ Promo Code Redemption
✅ Reward Withdrawal System
✅ Request Deduplication with KV TTL
✅ Edge-side Caching
✅ Serverless Scalability

## Architecture

```
Client (Android App)
        ↓
    HTTPS
        ↓
Cloudflare Edge Network
        ↓
    Hono Router
        ↓
    ┌─────────────────────────┐
    │  Route Handlers         │
    │ ├── /api/auth           │
    │ ├── /api/tasks          │
    │ ├── /api/offers         │
    │ ├── /api/referral       │
    │ ├── /api/coins          │
    │ ├── /api/promos         │
    │ └── /api/rewards        │
    └─────────────────────────┘
        ↓        ↓         ↓
      KV      R2(S3)  Admin API
    (Cache)  (Files)  (DynamoDB)
```

## Tech Stack

- **Runtime**: Cloudflare Workers (V8 Isolates)
- **Framework**: Hono (Lightweight REST framework)
- **Storage**: Cloudflare KV (distributed cache)
- **File Storage**: Cloudflare R2 (S3-compatible)
- **Deployment**: Wrangler CLI
- **Language**: TypeScript
- **Validation**: Zod

## Project Structure

```
earnzy-workers/
├── src/
│   ├── index.ts              # Main app entry
│   └── routes/
│       ├── auth.ts           # Authentication
│       ├── tasks.ts          # Task management
│       ├── offers.ts         # Offer claims
│       ├── referral.ts       # Referral system
│       ├── coins.ts          # Balance & history
│       ├── promos.ts         # Promo codes
│       └── rewards.ts        # Withdrawals
├── wrangler.toml             # Configuration
├── tsconfig.json
├── package.json
└── README.md
```

## Setup

### Prerequisites
- Node.js 18+
- Cloudflare account (free tier works)
- Wrangler CLI (`npm install -g wrangler`)

### Installation

1. **Clone repository**
```bash
git clone https://github.com/yourusername/earnzy-workers.git
cd earnzy-workers
```

2. **Install dependencies**
```bash
npm install
```

3. **Configure Wrangler** (in `wrangler.toml`)
```toml
name = "earnzy-api"
account_id = "your-account-id"
workers_dev = true
```

4. **Create KV namespace**
```bash
wrangler kv:namespace create "KV_CACHE"
wrangler kv:namespace create "KV_CACHE" --preview
```

5. **Set environment variables**
```bash
wrangler secret put ADMIN_API_URL
wrangler secret put JWT_SECRET
wrangler secret put FIREBASE_API_KEY
```

## API Documentation

### Authentication

#### Register / Login
```bash
POST /api/auth/register
Content-Type: application/json

{
  "uid": "user-uuid",
  "email": "user@example.com",
  "deviceToken": "fcm-token"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiI...",
  "user": {
    "uid": "user-uuid",
    "email": "user@example.com",
    "coins": 0,
    "level": 1
  }
}
```

#### Get Current User
```bash
GET /api/auth/me
Authorization: Bearer {token}

Response:
{
  "uid": "user-uuid",
  "email": "user@example.com",
  "coins": 1500,
  "level": 2
}
```

### Tasks

#### List All Tasks
```bash
GET /api/tasks

Response:
{
  "tasks": [
    {
      "id": "task-1",
      "title": "Watch Video",
      "description": "Watch a 30-second video",
      "category": "video",
      "reward": 10,
      "imageUrl": "https://...",
      "isActive": true
    }
  ],
  "meta": { "total": 25 }
}
```

#### Complete Task
```bash
POST /api/tasks/{taskId}/complete
Authorization: Bearer {token}
Content-Type: application/json

{
  "reward": 10
}

Response:
{
  "success": true,
  "reward": 10
}
```

### Offers

#### Get Offerwall
```bash
GET /api/offers/wall/list

Response:
{
  "wall": {
    "games": [
      { "id": "offer-1", "title": "Game Download", ... }
    ],
    "apps": [
      { "id": "offer-2", "title": "App Install", ... }
    ]
  }
}
```

#### Claim Offer
```bash
POST /api/offers/{offerId}/claim
Authorization: Bearer {token}

Response:
{
  "success": true,
  "message": "Offer claimed! Check your wallet."
}
```

### Referral

#### Get Referral Code
```bash
GET /api/referral/code
Authorization: Bearer {token}

Response:
{
  "code": "REF123456",
  "deeplink": "earnzy://ref/REF123456",
  "shareUrl": "https://earnzy.com/ref/REF123456"
}
```

#### Get Referral Stats
```bash
GET /api/referral/stats
Authorization: Bearer {token}

Response:
{
  "totalReferrals": 5,
  "earnedCoins": 250,
  "activeReferrals": 4,
  "referrals": [...]
}
```

### Coins

#### Get Balance
```bash
GET /api/coins/balance
Authorization: Bearer {token}

Response:
{
  "coins": 1500,
  "level": 2,
  "nextLevelCoins": 3000
}
```

#### Get Transaction History
```bash
GET /api/coins/history
Authorization: Bearer {token}

Response:
{
  "transactions": [
    {
      "id": "txn-1",
      "type": "earn",
      "amount": 10,
      "description": "Task Completed",
      "createdAt": "2024-01-20T10:30:00Z"
    }
  ],
  "total": 42
}
```

### Promo Codes

#### List Promo Codes
```bash
GET /api/promos

Response:
{
  "promoCodes": [
    {
      "id": "promo-1",
      "code": "NEW50",
      "reward": 50,
      "description": "New user bonus",
      "expiresAt": "2024-12-31T23:59:59Z",
      "isActive": true
    }
  ]
}
```

#### Redeem Promo Code
```bash
POST /api/promos/redeem
Authorization: Bearer {token}
Content-Type: application/json

{
  "code": "NEW50"
}

Response:
{
  "success": true,
  "reward": 50,
  "message": "You earned 50 coins!"
}
```

### Rewards / Withdrawals

#### Get Withdrawal Methods
```bash
GET /api/rewards

Response:
{
  "rewards": [
    {
      "id": "paytm",
      "name": "Paytm Wallet",
      "icon": "paytm",
      "minCoins": 100,
      "rewards": [
        { "amount": 100, "coins": 100 },
        { "amount": 500, "coins": 500 }
      ]
    }
  ]
}
```

#### Request Redemption
```bash
POST /api/rewards/request
Authorization: Bearer {token}
Content-Type: application/json

{
  "rewardId": "paytm",
  "amount": 100,
  "upiId": "user@paytm"
}

Response:
{
  "success": true,
  "requestId": "req_1234567890_abc123",
  "message": "Redemption request submitted. You will receive your reward within 24 hours."
}
```

#### Get Redemption History
```bash
GET /api/rewards/history
Authorization: Bearer {token}

Response:
{
  "redemptions": [
    {
      "id": "req_1234567890_abc123",
      "rewardId": "paytm",
      "amount": 100,
      "status": "completed",
      "requestedAt": "2024-01-20T10:00:00Z",
      "completedAt": "2024-01-20T11:30:00Z"
    }
  ]
}
```

## Deployment

### Local Development
```bash
npm run dev
# Server runs at http://localhost:8787
```

### Deploy to Cloudflare Workers

```bash
# Preview deployment
wrangler dev

# Production deployment
wrangler deploy

# Deploy to specific environment
wrangler deploy --env production
```

### Environment Variables

Set these in Cloudflare Workers Secrets:
```bash
wrangler secret put ADMIN_API_URL      # Your admin panel URL
wrangler secret put JWT_SECRET         # JWT signing secret
wrangler secret put FIREBASE_API_KEY   # Firebase project key
```

## Performance & Caching

### KV Caching Strategy

1. **User Sessions** (24 hours):
```
Key: token:{userId}
```

2. **Device Tokens** (30 days):
```
Key: device:{userId}
```

3. **Task/Offer Claims** (Prevent duplicates):
```
Key: task_complete:{userId}:{taskId}  // 24 hours
Key: offer_claim:{userId}:{offerId}   // 30 days
```

4. **Promo Usage** (365 days):
```
Key: promo_used:{userId}:{code}
```

### Edge-Side Optimization

- CORS headers cached at edge
- Health check response cached
- Request deduplication with KV TTL

## Monitoring & Logging

Access logs in:
- **Cloudflare Dashboard**: Analytics → Workers
- **Console logs**: `wrangler tail`

## Troubleshooting

### KV Not Available
```bash
# Check KV bindings in wrangler.toml
wrangler kv:key list --namespace-id YOUR_ID
```

### CORS Issues
- Check `Access-Control-Allow-Origin` headers
- Verify origin domain in wrangler.toml

### Timeout Errors
- Workers have 30s limit
- Optimize admin API calls
- Use KV for caching expensive operations

## Contributing

1. Create feature branch
2. Test locally: `npm run dev`
3. Deploy to preview
4. Submit pull request

## License

MIT License

## Support

- GitHub Issues: [Report bugs]
- Documentation: [See docs/](../docs/)
- Email: support@earnzy.com
