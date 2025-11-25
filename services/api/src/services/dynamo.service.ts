import {
  DynamoDBClient,
  PutItemCommand,
  GetItemCommand,
  QueryCommand,
  ScanCommand,
  UpdateItemCommand,
  DeleteItemCommand,
  BatchWriteItemCommand,
} from "@aws-sdk/client-dynamodb";
import { marshall, unmarshall } from "@aws-sdk/util-dynamodb";

const dynamoClient = new DynamoDBClient({
  region: process.env.AWS_REGION || "ap-south-1",
});

const TABLE_NAME = process.env.DYNAMODB_TABLE || "earnzy-admin";

interface DynamoItem {
  [key: string]: any;
}

export class DynamoService {
  /**
   * Create item with PK: TYPE#ID, SK: CREATED_AT#ID
   */
  static async createItem(
    itemType: string,
    itemId: string,
    data: DynamoItem
  ): Promise<DynamoItem> {
    const timestamp = new Date().toISOString();
    const item = {
      PK: `${itemType}#${itemId}`,
      SK: `${timestamp}#${itemId}`,
      TYPE: itemType,
      ID: itemId,
      CREATED_AT: timestamp,
      UPDATED_AT: timestamp,
      ...data,
    };

    const command = new PutItemCommand({
      TableName: TABLE_NAME,
      Item: marshall(item),
    });

    await dynamoClient.send(command);
    return item;
  }

  /**
   * Get item by PK
   */
  static async getItem(pk: string, sk?: string): Promise<DynamoItem | null> {
    const key: any = { PK: pk };
    if (sk) key.SK = sk;

    const command = new GetItemCommand({
      TableName: TABLE_NAME,
      Key: marshall(key),
    });

    const response = await dynamoClient.send(command);
    return response.Item ? unmarshall(response.Item) : null;
  }

  /**
   * Query items by PK (all items of same type)
   */
  static async queryByType(
    itemType: string,
    options: { limit?: number; exclusiveStartKey?: any } = {}
  ): Promise<DynamoItem[]> {
    const command = new QueryCommand({
      TableName: TABLE_NAME,
      KeyConditionExpression: "PK = :pk",
      ExpressionAttributeValues: marshall({
        ":pk": `${itemType}#*`,
      }),
      Limit: options.limit || 100,
      ExclusiveStartKey: options.exclusiveStartKey
        ? marshall(options.exclusiveStartKey)
        : undefined,
    });

    const response = await dynamoClient.send(command);
    return response.Items
      ? response.Items.map((item) => unmarshall(item))
      : [];
  }

  /**
   * Query by GSI (e.g., email lookup)
   */
  static async queryByGSI(
    indexName: string,
    partitionKey: string,
    partitionValue: string,
    options: { limit?: number } = {}
  ): Promise<DynamoItem[]> {
    const command = new QueryCommand({
      TableName: TABLE_NAME,
      IndexName: indexName,
      KeyConditionExpression: `${partitionKey} = :pk`,
      ExpressionAttributeValues: marshall({
        ":pk": partitionValue,
      }),
      Limit: options.limit || 100,
    });

    const response = await dynamoClient.send(command);
    return response.Items
      ? response.Items.map((item) => unmarshall(item))
      : [];
  }

  /**
   * Scan table (use sparingly for large tables)
   */
  static async scan(
    filterExpression?: string,
    expressionAttributeValues?: any
  ): Promise<DynamoItem[]> {
    const command = new ScanCommand({
      TableName: TABLE_NAME,
      FilterExpression: filterExpression,
      ExpressionAttributeValues: expressionAttributeValues
        ? marshall(expressionAttributeValues)
        : undefined,
    });

    const response = await dynamoClient.send(command);
    return response.Items
      ? response.Items.map((item) => unmarshall(item))
      : [];
  }

  /**
   * Update item (partial update)
   */
  static async updateItem(
    pk: string,
    sk: string,
    updates: DynamoItem
  ): Promise<DynamoItem> {
    const updateExpressions: string[] = [];
    const expressionAttributeValues: any = {};
    let counter = 0;

    for (const [key, value] of Object.entries(updates)) {
      updateExpressions.push(`${key} = :val${counter}`);
      expressionAttributeValues[`:val${counter}`] = value;
      counter++;
    }

    updateExpressions.push(`UPDATED_AT = :updated`);
    expressionAttributeValues[":updated"] = new Date().toISOString();

    const command = new UpdateItemCommand({
      TableName: TABLE_NAME,
      Key: marshall({ PK: pk, SK: sk }),
      UpdateExpression: `SET ${updateExpressions.join(", ")}`,
      ExpressionAttributeValues: marshall(expressionAttributeValues),
      ReturnValues: "ALL_NEW",
    });

    const response = await dynamoClient.send(command);
    return response.Attributes ? unmarshall(response.Attributes) : {};
  }

  /**
   * Delete item
   */
  static async deleteItem(pk: string, sk: string): Promise<void> {
    const command = new DeleteItemCommand({
      TableName: TABLE_NAME,
      Key: marshall({ PK: pk, SK: sk }),
    });

    await dynamoClient.send(command);
  }

  /**
   * Batch write items
   */
  static async batchWrite(items: DynamoItem[]): Promise<void> {
    const writeRequests = items.map((item) => ({
      PutRequest: {
        Item: marshall(item),
      },
    }));

    const command = new BatchWriteItemCommand({
      RequestItems: {
        [TABLE_NAME]: writeRequests,
      },
    });

    await dynamoClient.send(command);
  }
}
