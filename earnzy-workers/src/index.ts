import { Hono } from 'hono';
import { cors } from 'hono/cors';
import { logger } from 'hono/logger';
import authRoutes from './routes/auth';
import tasksRoutes from './routes/tasks';
import offersRoutes from './routes/offers';
import referralRoutes from './routes/referral';
import coinsRoutes from './routes/coins';
import promosRoutes from './routes/promos';
import rewardsRoutes from './routes/rewards';

interface Env {
  BUCKET: R2Bucket;
  KV_CACHE: KVNamespace;
  ADMIN_API_URL: string;
  JWT_SECRET: string;
  FIREBASE_API_KEY: string;
}

const app = new Hono<{ Bindings: Env }>();

// Middleware
app.use(logger());
app.use('*', cors({
  origin: '*',
  allowMethods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
  allowHeaders: ['Content-Type', 'Authorization'],
}));

// Health check
app.get('/health', (c) => c.json({ status: 'ok', timestamp: new Date().toISOString() }));

// Routes
app.route('/api/auth', authRoutes);
app.route('/api/tasks', tasksRoutes);
app.route('/api/offers', offersRoutes);
app.route('/api/referral', referralRoutes);
app.route('/api/coins', coinsRoutes);
app.route('/api/promos', promosRoutes);
app.route('/api/rewards', rewardsRoutes);

// 404 handler
app.notFound((c) => c.json({ error: 'Not found' }, 404));

// Error handler
app.onError((err, c) => {
  console.error('Error:', err);
  return c.json({ error: 'Internal server error' }, 500);
});

export default app;
