import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {HomeComponent} from './view/home/home.component';
import {SearchComponent} from './view/search/search.component';
import {DashboardStatisticsComponent} from './view/dashboard-statistics/dashboard-statistics.component';
import {SearchResolver} from './shared/search/resolver/search.resolver';
import {StatisticsResolver} from './shared/statistics/resolver/statistics-resolver';
import {DebugDocumentComponent} from './view/debug-document/debug-document.component';
import {ApplicationResolver} from "./shared/application/resolver/application-resolver";
import {DashboardApplicationsComponent} from "./view/dashboard-applications/dashboard-applications.component";

const routes: Routes = [
  {path: '', redirectTo: '/home', pathMatch: 'full'},
  {path: 'home', component: HomeComponent},
  {path: 'search', component: SearchComponent, resolve: {statistics: SearchResolver}},
  {path: 'dashboard/statistics', component: DashboardStatisticsComponent, resolve: {statistics: StatisticsResolver}},
  {path: 'dashboard/applications', component: DashboardApplicationsComponent, resolve: {applications: ApplicationResolver}},
  {path: 'debug-document', component: DebugDocumentComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: true})],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
