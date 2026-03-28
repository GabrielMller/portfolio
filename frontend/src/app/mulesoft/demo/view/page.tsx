import Pagination from "@/components/Pagination";
import { itemsApi } from "@/lib/mulesoft-client";
import {
  Card,
  CardActions,
  CardContent,
  CardMedia,
  Container,
  Fade,
  Grid,
  Tooltip,
  Typography,
} from "@mui/material";
import { cookies } from "next/headers";
import { unauthorized } from "next/navigation";
import MulesoftAddToCartButton from "@/components/MulesoftAddToCartButton";
import Image from "next/image";

const PAGE_SIZE = 15;

export default async function Page({
  searchParams,
}: {
  searchParams: { [key: string]: string | undefined };
}) {
  const cookiesStore = await cookies();
  const token = cookiesStore.get("authjs.session-token")?.value || cookiesStore.get("__Secure-authjs.session-token")?.value;

  if (!token) {
    return unauthorized();
  }

  const params = await searchParams;

  const page = params.page ? parseInt(params.page) : 1;

  const initialItems = await itemsApi.getItems(token, page, PAGE_SIZE);
  return (
    <Container fixed sx={{ py: 4 }}>
      <Grid
        columns={{
          xs: 12,
          md: 12,
          lg: 10,
        }}
        container
        spacing={2}
      >
        {initialItems.data.map((item) => (
          <Grid
            size={{
              xs: 6,
              md: 4,
              lg: 2,
            }}
            key={item.id}
          >
            <Fade in={true}>
              <Card
                sx={{
                  height: "100%",
                  border: "1px solid",
                  borderColor: "divider",
                  transition: "0.2s",
                  display: "flex",
                  flexDirection: "column",
                  "&:hover": {
                    borderColor: "primary.main",
                    transform: "scale(1.02)",
                  },
                }}
              >
                <CardMedia>
                  <Image
                    src={item.image}
                    alt={item.name}
                    width={500}
                    height={500}
                    style={{
                      aspectRatio: "1 / 1",
                      objectFit: "cover",
                      width: "100%",
                    }}
                  />
                </CardMedia>
                <CardContent>
                  <Tooltip title={item.name}>
                    <Typography noWrap variant="body2">
                      {item.name}
                    </Typography>
                  </Tooltip>
                  <Typography variant="body2" color="text.secondary">
                    {Number(item.price).toLocaleString("pt-BR", {
                      style: "currency",
                      currency: "BRL",
                    })}
                  </Typography>
                </CardContent>
                <CardActions
                  sx={{
                    mt: "auto",
                    display: "flex",
                    justifyContent: "flex-end",
                  }}
                >
                  <MulesoftAddToCartButton item={item} />
                </CardActions>
              </Card>
            </Fade>
          </Grid>
        ))}
        <Grid size={12} sx={{ display: "flex", justifyContent: "center" }}>
          <Pagination
            currentPage={initialItems.metadata.page}
            totalPages={Math.ceil(
              initialItems.metadata.totalItems / initialItems.metadata.pageSize,
            )}
          />
        </Grid>
      </Grid>
    </Container>
  );
}
