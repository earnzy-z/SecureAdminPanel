export function isUnauthorizedError(error: Error): boolean {
  return /^401: .*Unauthorized|401.*Unauthorized/.test(error.message);
}
