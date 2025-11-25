import admin from "firebase-admin";

// Initialize Firebase Admin SDK
const serviceAccountKey = JSON.parse(
  process.env.FIREBASE_SERVICE_ACCOUNT || "{}"
);

if (Object.keys(serviceAccountKey).length > 0) {
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccountKey),
  });
}

export class FCMService {
  /**
   * Send single notification to user
   */
  static async sendToUser(
    userId: string,
    deviceTokens: string[],
    message: {
      title: string;
      body: string;
      imageUrl?: string;
      data?: Record<string, string>;
    }
  ): Promise<void> {
    const payload: any = {
      notification: {
        title: message.title,
        body: message.body,
      },
      android: {
        priority: "high",
        notification: {
          sound: "default",
          clickAction: "FLUTTER_NOTIFICATION_CLICK",
        },
      },
    };

    if (message.imageUrl) {
      payload.notification.imageUrl = message.imageUrl;
    }

    if (message.data) {
      payload.data = message.data;
    }

    for (const token of deviceTokens) {
      try {
        await admin.messaging().send({
          ...payload,
          token,
        });
      } catch (error) {
        console.error(`Failed to send FCM to token ${token}:`, error);
      }
    }
  }

  /**
   * Send bulk notification to multiple users
   */
  static async sendBulk(
    deviceTokens: string[],
    message: {
      title: string;
      body: string;
      imageUrl?: string;
      data?: Record<string, string>;
    }
  ): Promise<void> {
    const payload: any = {
      notification: {
        title: message.title,
        body: message.body,
      },
      android: {
        priority: "high",
        notification: {
          sound: "default",
        },
      },
    };

    if (message.imageUrl) {
      payload.notification.imageUrl = message.imageUrl;
    }

    if (message.data) {
      payload.data = message.data;
    }

    const results = await admin.messaging().sendMulticast({
      ...payload,
      tokens: deviceTokens,
    });

    console.log(`Sent ${results.successCount} messages, ${results.failureCount} failed`);
  }

  /**
   * Subscribe user to topic (for topic-based messaging)
   */
  static async subscribeToTopic(
    tokens: string[],
    topic: string
  ): Promise<void> {
    await admin.messaging().subscribeToTopic(tokens, topic);
  }

  /**
   * Unsubscribe from topic
   */
  static async unsubscribeFromTopic(
    tokens: string[],
    topic: string
  ): Promise<void> {
    await admin.messaging().unsubscribeFromTopic(tokens, topic);
  }
}
