import {Component} from '@angular/core';
import {
  Event,
  NavigationCancel,
  NavigationEnd,
  NavigationError,
  NavigationStart,
  Router
} from '@angular/router';
import {SiteInfoService} from "../../shared/info/service/site-info.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'loa-frontend';
  loading = false;
  usersEnabled = false;

  constructor(private router: Router, private siteInfoService: SiteInfoService) {
    siteInfoService.querySiteInfo().subscribe(siteInfo => {
      this.usersEnabled = siteInfo.usersEnabled;

      console.log("Users enabled: " + this.usersEnabled);
    })

    this.router.events.subscribe((event: Event) => {
      console.log('New route change event!', event);

      switch (true) {
        case event instanceof NavigationStart: {
          this.loading = true;
          break;
        }

        case event instanceof NavigationEnd:
        case event instanceof NavigationCancel:
        case event instanceof NavigationError: {
          this.loading = false;
          break;
        }
        default: {
          break;
        }
      }
    });
  }
}
