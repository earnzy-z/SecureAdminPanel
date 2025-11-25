import fs from "fs";
import path from "path";

const logDir = path.join(process.cwd(), "logs");

if (!fs.existsSync(logDir)) {
  fs.mkdirSync(logDir, { recursive: true });
}

interface LogEntry {
  timestamp: string;
  level: "INFO" | "WARN" | "ERROR" | "DEBUG";
  message: string;
  meta?: any;
}

export class Logger {
  static log(message: string, meta?: any) {
    const entry: LogEntry = {
      timestamp: new Date().toISOString(),
      level: "INFO",
      message,
      meta,
    };
    console.log(JSON.stringify(entry));
    this.writeToFile(entry);
  }

  static warn(message: string, meta?: any) {
    const entry: LogEntry = {
      timestamp: new Date().toISOString(),
      level: "WARN",
      message,
      meta,
    };
    console.warn(JSON.stringify(entry));
    this.writeToFile(entry);
  }

  static error(message: string, error?: any) {
    const entry: LogEntry = {
      timestamp: new Date().toISOString(),
      level: "ERROR",
      message,
      meta: error,
    };
    console.error(JSON.stringify(entry));
    this.writeToFile(entry);
  }

  private static writeToFile(entry: LogEntry) {
    const logFile = path.join(logDir, `${new Date().toISOString().split("T")[0]}.log`);
    fs.appendFileSync(logFile, JSON.stringify(entry) + "\n");
  }
}
