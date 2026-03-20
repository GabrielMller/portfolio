import { itemsApi } from "@/lib/mulesoft-client";
import {
  Card,
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

export default async function Page() {
  const token = (await cookies()).get("authjs.session-token")?.value;

  if (!token) {
    return unauthorized();
  }

  const initialItems = await itemsApi.getItems(token);
  return (
    <Container sx={{ py: 4 }}>
      <Grid columns={{
        xs: 12,
        md: 12,
        lg: 10,
      }} container spacing={2}>
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
                <CardMedia component="img" sx={{ 
                              aspectRatio: '1 / 1', 
                              objectFit: 'cover',
                              width: '100%'
                            }} image={item.image} alt={item.name} />
                <CardContent>
                  <Tooltip title={item.name}>
                    <Typography noWrap variant="body2">
                      {item.name}
                    </Typography>
                  </Tooltip>
                  <Typography variant="body2" color="text.secondary">
                    {Number(item.price).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}
                  </Typography>
                </CardContent>
              </Card>
            </Fade>
          </Grid>
        ))}
      </Grid>
    </Container>
  );
}
