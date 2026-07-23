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
    // Satış akışı da route parametresine bağlı; istek anında sunucuda render edilir.
    path: 'customers/:id/new-sale',
    renderMode: RenderMode.Server
  },
  {
    path: '**',
    renderMode: RenderMode.Prerender
  }
];
