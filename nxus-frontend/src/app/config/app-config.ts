export const AppConfig = {
  apiBaseUrl: (window as any).__APP_API_BASE_URL__ ?? '/api'
};

export const API = {
  nlp: () => `${AppConfig.apiBaseUrl}/nlp`,
  gptAsk: () => `${AppConfig.apiBaseUrl}/gpt/ask`,
  spotify: {
    login: () => `${AppConfig.apiBaseUrl}/spotify/login`,
    callback: (code: string) => `${AppConfig.apiBaseUrl}/spotify/callback?code=${encodeURIComponent(code)}`,
    search: () => `${AppConfig.apiBaseUrl}/spotify/search`,
    play: () => `${AppConfig.apiBaseUrl}/spotify/play`,
    playPlaylist: () => `${AppConfig.apiBaseUrl}/spotify/playplaylist`,
    pause: () => `${AppConfig.apiBaseUrl}/spotify/pause`,
    next: () => `${AppConfig.apiBaseUrl}/spotify/next`,
    previous: () => `${AppConfig.apiBaseUrl}/spotify/previous`
  }
};
