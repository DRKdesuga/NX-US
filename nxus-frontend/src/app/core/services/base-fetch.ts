import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class BaseFetch {
  private async request<T>(url: string, init: RequestInit = {}, timeoutMs = 20000): Promise<T> {
    const headers = new Headers(init.headers as HeadersInit | undefined);
    if (!headers.has('Accept')) headers.set('Accept', 'application/json');
    const hasBody = init.body !== undefined && init.body !== null;
    if (hasBody && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json');

    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), timeoutMs);

    let res: Response;
    try {
      res = await fetch(url, { ...init, headers, signal: controller.signal });
    } catch (e: any) {
      clearTimeout(timer);
      if (e?.name === 'AbortError') throw new Error('Request timed out.');
      throw new Error('Network error. Backend not reachable or CORS blocked.');
    }
    clearTimeout(timer);

    const ct = res.headers.get('content-type') || '';
    const isJson = ct.includes('application/json');
    let data: any = null;
    try { data = isJson ? await res.json() : await res.text(); } catch {}

    if (!res.ok) {
      const msg = (isJson ? (data?.message || data?.error?.message) : String(data || res.statusText)) || res.statusText || 'Unknown error';
      if (res.status === 404 && /no active device/i.test(msg)) throw new Error('No active Spotify device. Open a Spotify client and try again.');
      throw new Error(`${res.status} ${msg}`);
    }
    return (isJson ? data : (data as unknown as T));
  }

  get<T>(url: string, headers?: Record<string, string>) { return this.request<T>(url, { method: 'GET', headers }); }
  post<T>(url: string, body?: unknown, headers?: Record<string, string>) {
    const payload = body !== undefined ? JSON.stringify(body) : undefined;
    return this.request<T>(url, { method: 'POST', body: payload, headers });
  }
}
