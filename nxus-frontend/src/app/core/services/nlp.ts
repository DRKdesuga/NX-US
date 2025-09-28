import { Injectable } from '@angular/core';
import { BaseFetch } from './base-fetch';
import { API } from '../../config/app-config';

export interface NlpResult {
  intent?: string | null;
  slots?: Record<string, unknown> | null;
  rawText: string;
  confidence?: number | null;
}

@Injectable({ providedIn: 'root' })
export class Nlp {
  constructor(private http: BaseFetch) {}
  analyze(rawText: string): Promise<NlpResult> {
    return this.http.post(API.nlp(), { rawText });
  }
}
