import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {HomeComponent} from './view/home/home.component';
import {SearchComponent} from './view/search/search.component';
import {DashboardComponent} from './view/dashboard/dashboard.component';
import {SearchResolver} from './shared/search/resolver/search.resolver';
import {StatisticsResolver} from './statistics.resolver';
import {DebugDocumentComponent} from './view/debug-document/debug-document.component';

const routes: Routes = [
  {path: '', redirectTo: '/home', pathMatch: 'full'},
  {path: 'home', component: HomeComponent},
  {path: 'search', component: SearchComponent, resolve: {statistics: SearchResolver}},
  {path: 'dashboard', component: DashboardComponent, resolve: {statistics: StatisticsResolver}},
  {path: 'debug-document', component: DebugDocumentComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: true})],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
