import {inject, NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {HomeComponent} from './view/home/home.component';
import {SearchComponent} from './view/search/search.component';
import {DashboardStatisticsComponent} from './view/dashboard-statistics/dashboard-statistics.component';
import {DebugDocumentComponent} from './view/debug-document/debug-document.component';
import {DebugLocationComponent} from './view/debug-location/debug-location.component';
import {DashboardApplicationsComponent} from './view/dashboard-applications/dashboard-applications.component';
import {LoginComponent} from './view/login/login.component';
import {StatisticsService} from './shared/statistics/service/statistics-service';
import {SearchService} from './shared/search/service/search.service';
import {ApplicationService} from './shared/application/service/application.service';

const routes: Routes = [
  {path: '', redirectTo: '/home', pathMatch: 'full'},
  {path: 'home', component: HomeComponent},
  {path: 'login', component: LoginComponent},
  {
    path: 'search',
    component: SearchComponent,
    resolve: {statistics: () => inject(SearchService).getSearchStatistics()}
  },
  {
    path: 'dashboard/statistics',
    component: DashboardStatisticsComponent,
    resolve: {statistics: () => inject(StatisticsService).getStatistics()}
  },
  {
    path: 'dashboard/applications',
    component: DashboardApplicationsComponent,
    resolve: {applications: () => inject(ApplicationService).getApplications()}
  },
  {path: 'debug-document', component: DebugDocumentComponent},
  {path: 'debug-location', component: DebugLocationComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: true})],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
