import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  /**
   * Müşteri detayı route parametresine bağlı olduğu için önceden üretilemez;
   * istek anında sunucuda render edilir.
   */
  {
    path: 'customers/:id',
    renderMode: RenderMode.Server
  },
  {
    path: '**',
    renderMode: RenderMode.Prerender
  }
];
