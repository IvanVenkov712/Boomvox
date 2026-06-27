import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SidebarBrowser } from './sidebar-browser';

describe('SidebarBrowser', () => {
  let component: SidebarBrowser;
  let fixture: ComponentFixture<SidebarBrowser>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SidebarBrowser],
    }).compileComponents();

    fixture = TestBed.createComponent(SidebarBrowser);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
