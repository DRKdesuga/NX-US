import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConnectSpotify } from './connect-spotify';

describe('ConnectSpotify', () => {
  let component: ConnectSpotify;
  let fixture: ComponentFixture<ConnectSpotify>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConnectSpotify]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ConnectSpotify);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
