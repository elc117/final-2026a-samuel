export function getAuthRedirect(search: string): string {
  const redirect = new URLSearchParams(search).get("redirect");

  if (!redirect || !redirect.startsWith("/") || redirect.startsWith("//")) {
    return "/grupo";
  }

  return redirect;
}

export function buildAuthPath(path: "/login" | "/cadastro", redirect: string) {
  return `${path}?redirect=${encodeURIComponent(redirect)}`;
}
