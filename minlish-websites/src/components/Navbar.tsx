import { useEffect, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { BookOpen, BarChart3, Home, Bell, LogIn, LogOut, CircleUser, ChevronDown } from 'lucide-react';
import { useAuth } from '@/hooks/useAuth';
import { Button } from '@/components/ui/button';
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu';
import { getUnreadNotificationCount, getLearningPlan } from '@/lib/api';

const navItems = [
  { to: '/dashboard', label: 'Tiến độ', icon: BarChart3 },
  { to: '/notifications', label: 'Thông báo', icon: Bell },
  { to: '/profile', label: 'Hồ sơ', icon: CircleUser },
];

export default function Navbar() {
  const { pathname } = useLocation();
  const { user, loading, signOut } = useAuth();
  const [unreadCount, setUnreadCount] = useState(0);
  const [dueReviewCount, setDueReviewCount] = useState(0);
  const hasBackendToken = !!(localStorage.getItem('accessToken') || localStorage.getItem('authToken'));
  const showAccountActions = !!(user || hasBackendToken);
  const storedDisplayName =
    localStorage.getItem('fullName') ||
    localStorage.getItem('name') ||
    localStorage.getItem('username') ||
    '';
  const userDisplayName = (user?.fullName || storedDisplayName || 'bạn').trim();

  useEffect(() => {
    let cancelled = false;

    const refreshUnread = async () => {
      if (loading) {
        return;
      }

      if (!showAccountActions) {
        setUnreadCount(0);
        return;
      }
      try {
        const unreadCount = await getUnreadNotificationCount();
        if (!cancelled) {
          setUnreadCount(unreadCount);
        }
      } catch {
        if (!cancelled) {
          setUnreadCount(0);
        }
      }
    };

    const refreshDueReview = async () => {
      if (!showAccountActions) {
        setDueReviewCount(0);
        return;
      }
      try {
        const plan = await getLearningPlan();
        if (!cancelled) {
          setDueReviewCount(plan.todayReviewWordsDue || 0);
        }
      } catch {
        if (!cancelled) {
          setDueReviewCount(0);
        }
      }
    };

    const onUpdated = () => {
      void refreshUnread();
    };
    const onReviewUpdated = () => {
      void refreshDueReview();
    };

    void refreshUnread();
    void refreshDueReview();
    window.addEventListener('notifications:updated', onUpdated);
    window.addEventListener('review:updated', onReviewUpdated);

    return () => {
      cancelled = true;
      window.removeEventListener('notifications:updated', onUpdated);
      window.removeEventListener('review:updated', onReviewUpdated);
    };
  }, [showAccountActions, pathname, loading]);

  const handleSignOut = async () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('authToken');
    localStorage.removeItem('userId');
    localStorage.removeItem('email');
    localStorage.removeItem('fullName');
    localStorage.removeItem('name');
    localStorage.removeItem('username');
    await signOut();
    window.location.assign('/auth');
  };

  return (
    <header className="sticky top-0 z-50 border-b border-border bg-card/80 px-4 backdrop-blur-lg">
      <div className="container mx-auto flex h-16 items-center justify-between">
        <Link to="/" className="flex items-center">
          <img
            src="/logo_minlish.png"
            alt="MinLish"
            className="block h-11 w-auto object-contain transition-opacity duration-200 hover:opacity-95"
          />
        </Link>

        <nav className="hidden items-center gap-1 md:flex">
          <Link
            to="/"
            className={`flex items-center gap-2 rounded-xl px-4 py-2 text-sm font-medium transition-all duration-200 ${
              pathname === '/'
                ? 'bg-primary/14 text-primary shadow-sm ring-1 ring-primary/20'
                : 'text-muted-foreground hover:bg-primary/10 hover:text-primary hover:shadow-sm'
            }`}
          >
            <Home className="h-4 w-4" />
            <span>Trang chủ</span>
          </Link>

          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <button
                type="button"
                className={`relative flex items-center gap-2 rounded-xl px-4 py-2 text-sm font-medium transition-all duration-200 ${
                  pathname.startsWith('/sets')
                    ? 'bg-primary/14 text-primary shadow-sm ring-1 ring-primary/20'
                    : 'text-muted-foreground hover:bg-primary/10 hover:text-primary hover:shadow-sm'
                }`}
              >
                <BookOpen className="h-4 w-4" />
                <span className="flex items-center gap-1">
                  Học tập
                  {dueReviewCount > 0 && (
                    <span className="ml-1 inline-flex min-w-[20px] items-center justify-center rounded-full bg-destructive px-1.5 py-0.5 text-xs font-semibold leading-none text-destructive-foreground">
                      {dueReviewCount > 99 ? '99+' : dueReviewCount}
                    </span>
                  )}
                </span>
                <ChevronDown className="h-4 w-4 ml-1" />
              </button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="start" className="w-48">
              <DropdownMenuItem asChild className="hover:bg-primary/20 hover:text-primary focus:bg-primary/20 focus:text-primary data-[highlighted]:bg-primary/20 data-[highlighted]:text-primary">
                <Link className="block w-full" to="/sets?tab=sets">Bộ từ vựng</Link>
              </DropdownMenuItem>
              <DropdownMenuItem asChild className="hover:bg-primary/20 hover:text-primary focus:bg-primary/20 focus:text-primary data-[highlighted]:bg-primary/20 data-[highlighted]:text-primary relative">
                <Link className="block w-full pr-8" to="/sets?tab=plan">
                  Kế hoạch học tập
                  {dueReviewCount > 0 && (
                    <span className="absolute right-2 top-1/2 -translate-y-1/2 inline-flex min-w-[18px] items-center justify-center rounded-full bg-destructive px-1 py-0.5 text-[11px] font-semibold leading-none text-destructive-foreground border border-white shadow">
                      {dueReviewCount > 99 ? '99+' : dueReviewCount}
                    </span>
                  )}
                </Link>
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>

          {navItems.map(({ to, label, icon: Icon }) => (
            <Link
              key={to}
              to={to}
              className={`flex items-center gap-2 rounded-xl px-4 py-2 text-sm font-medium transition-all duration-200 ${
                pathname === to
                  ? 'bg-primary/14 text-primary shadow-sm ring-1 ring-primary/20'
                  : 'text-muted-foreground hover:bg-primary/10 hover:text-primary hover:shadow-sm'
              }`}
            >
              <Icon className="h-4 w-4" />
              <span>{label}</span>
              {label === 'Thông báo' && unreadCount > 0 && (
                <span className="ml-1 inline-flex min-w-[20px] items-center justify-center rounded-full bg-destructive px-1.5 py-0.5 text-xs font-semibold leading-none text-destructive-foreground">
                  {unreadCount > 99 ? '99+' : unreadCount}
                </span>
              )}
            </Link>
          ))}
        </nav>

        <div className="hidden md:flex">
          {showAccountActions ? (
            <div className="flex items-center gap-3">
              <span className="max-w-[180px] truncate text-sm font-medium text-foreground/80" title={userDisplayName}>
                Xin chào, {userDisplayName}
              </span>
              <Button
                variant="outline"
                size="sm"
                onClick={handleSignOut}
                className="border-accent/40 bg-accent/20 text-amber-800 hover:text-primary-foreground"
              >
                <LogOut className="mr-2 h-4 w-4" /> Đăng xuất
              </Button>
            </div>
          ) : (
            <Button asChild variant="outline" size="sm" className="border-accent/40 bg-accent/20 text-amber-800 hover:text-primary-foreground">
              <Link to="/auth"><LogIn className="mr-2 h-4 w-4" /> Đăng nhập</Link>
            </Button>
          )}
        </div>

        {/* Mobile nav */}
        <nav className="fixed bottom-0 left-0 right-0 z-50 border-t border-border/70 bg-card/95 px-1 py-1.5 shadow-[0_-8px_24px_rgba(15,23,42,0.08)] backdrop-blur md:hidden">
          <div className="mx-auto grid w-full max-w-xl grid-cols-5 items-stretch gap-0.5">
          {/* Trang chủ */}
          <Link
            to="/"
            className={`relative flex flex-col items-center justify-center gap-1 rounded-xl py-2 text-[11px] font-semibold transition-all ${pathname === '/' ? 'bg-primary/10 text-primary' : 'text-muted-foreground hover:bg-primary/5 hover:text-primary'}`}
            style={{ minWidth: 0 }}
          >
            <Home className="h-5 w-5 mb-0.5" />
            <span className="truncate">Trang chủ</span>
          </Link>

          {/* Học - Dropdown */}
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <button
                className={`relative flex w-full flex-col items-center justify-center gap-1 rounded-xl py-2 text-[11px] font-semibold transition-all ${pathname.startsWith('/sets') ? 'bg-primary/10 text-primary' : 'text-muted-foreground hover:bg-primary/5 hover:text-primary'}`}
                style={{ minWidth: 0 }}
              >
                <BookOpen className="h-5 w-5 mb-0.5" />
                <span className="flex items-center gap-0.5 truncate">
                  Học tập
                  <ChevronDown className="h-3 w-3 ml-0.5" />
                </span>
                {dueReviewCount > 0 && (
                  <span className="absolute right-2 top-1 inline-flex min-w-[16px] items-center justify-center rounded-full bg-destructive px-1 py-0.5 text-[10px] font-semibold leading-none text-destructive-foreground">
                    {dueReviewCount > 99 ? '99+' : dueReviewCount}
                  </span>
                )}
              </button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="center" className="w-52 rounded-2xl border border-border/70 p-1.5 shadow-xl">
              <DropdownMenuItem asChild className="rounded-xl px-3 py-2.5 hover:bg-primary/15 hover:text-primary focus:bg-primary/15 focus:text-primary data-[highlighted]:bg-primary/15 data-[highlighted]:text-primary">
                <Link to="/sets?tab=sets" className="flex w-full items-center gap-2.5 text-sm">
                  <BookOpen className="h-4 w-4" />
                  <span>Bộ từ vựng</span>
                </Link>
              </DropdownMenuItem>
              <DropdownMenuItem asChild className="rounded-xl px-3 py-2.5 hover:bg-primary/15 hover:text-primary focus:bg-primary/15 focus:text-primary data-[highlighted]:bg-primary/15 data-[highlighted]:text-primary">
                <Link to="/sets?tab=plan" className="flex w-full items-center gap-2.5 text-sm">
                  <BarChart3 className="h-4 w-4" />
                  <span>Kế hoạch học tập</span>
                  {dueReviewCount > 0 && (
                    <span className="ml-auto inline-flex min-w-[16px] items-center justify-center rounded-full bg-destructive px-1 py-0.5 text-[10px] font-semibold leading-none text-destructive-foreground">
                      {dueReviewCount > 99 ? '99+' : dueReviewCount}
                    </span>
                  )}
                </Link>
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>

          {/* Tiến độ */}
          <Link
            to="/dashboard"
            className={`relative flex flex-col items-center justify-center gap-1 rounded-xl py-2 text-[11px] font-semibold transition-all ${pathname === '/dashboard' ? 'bg-primary/10 text-primary' : 'text-muted-foreground hover:bg-primary/5 hover:text-primary'}`}
            style={{ minWidth: 0 }}
          >
            <BarChart3 className="h-5 w-5 mb-0.5" />
            <span className="truncate">Tiến độ</span>
          </Link>

          {/* Thông báo */}
          <Link
            to="/notifications"
            className={`relative flex flex-col items-center justify-center gap-1 rounded-xl py-2 text-[11px] font-semibold transition-all ${pathname === '/notifications' ? 'bg-primary/10 text-primary' : 'text-muted-foreground hover:bg-primary/5 hover:text-primary'}`}
            style={{ minWidth: 0 }}
          >
            <Bell className="h-5 w-5 mb-0.5" />
            <span className="truncate">Thông báo</span>
            {unreadCount > 0 && (
              <span className="absolute right-2 top-1 inline-flex min-w-[16px] items-center justify-center rounded-full bg-destructive px-1 py-0.5 text-[10px] font-semibold leading-none text-destructive-foreground">
                {unreadCount > 99 ? '99+' : unreadCount}
              </span>
            )}
          </Link>

          {/* Hồ sơ + Tài khoản */}
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <button
                className={`flex w-full flex-col items-center justify-center gap-1 rounded-xl py-2 text-[11px] font-semibold transition-all ${pathname === '/profile' ? 'bg-primary/10 text-primary' : 'text-muted-foreground hover:bg-primary/5 hover:text-primary'}`}
                style={{ minWidth: 0 }}
              >
                <CircleUser className="h-5 w-5 mb-0.5" />
                <span className="truncate">Hồ sơ</span>
              </button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="w-52 rounded-2xl border border-border/70 p-1.5 shadow-xl">
              <DropdownMenuItem asChild className="rounded-xl px-3 py-2.5 hover:bg-primary/15 hover:text-primary focus:bg-primary/15 focus:text-primary data-[highlighted]:bg-primary/15 data-[highlighted]:text-primary">
                <Link className="flex w-full items-center gap-2.5" to={showAccountActions ? '/profile' : '/auth'}>
                  {showAccountActions ? (
                    <>
                      <CircleUser className="h-4 w-4" />
                      <span>Hồ sơ</span>
                    </>
                  ) : (
                    <>
                      <LogIn className="h-4 w-4" />
                      <span>Đăng nhập</span>
                    </>
                  )}
                </Link>
              </DropdownMenuItem>
              {showAccountActions && (
                <DropdownMenuItem
                  className="cursor-pointer rounded-xl px-3 py-2.5 text-destructive hover:bg-destructive/10 focus:bg-destructive/10 data-[highlighted]:bg-destructive/10"
                  onClick={handleSignOut}
                >
                  <LogOut className="mr-2 h-4 w-4" />
                  <span>Đăng xuất</span>
                </DropdownMenuItem>
              )}
            </DropdownMenuContent>
          </DropdownMenu>
          </div>
        </nav>
      </div>
    </header>
  );
}
