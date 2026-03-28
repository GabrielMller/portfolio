'use client';

import { Pagination as MuiPagination } from "@mui/material";
import { useRouter, useSearchParams } from "next/navigation";

export default function Pagination({ currentPage, totalPages }: { currentPage: number; totalPages: number; }) {
  const router = useRouter();
  const searchParams = useSearchParams();

  const onPageChange = (page: number) => {
    const params = new URLSearchParams(searchParams.toString());
    params.set("page", page.toString());
    router.push(`?${params.toString()}`);
  }

  return (
    <MuiPagination
      count={totalPages}
      page={Number(currentPage)}
      onChange={(_, page) => onPageChange(page)}
      color="primary"
      sx={{ display: "flex", justifyContent: "center", mt: 4 }}
    />
  )
}