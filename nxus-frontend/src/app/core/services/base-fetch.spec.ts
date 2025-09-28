import { TestBed } from '@angular/core/testing';

import { BaseFetch } from './base-fetch';

describe('BaseFetch', () => {
  let service: BaseFetch;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(BaseFetch);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
