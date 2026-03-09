import { cookies } from 'next/headers';

export default async function Teste({req}: {req: Request}) {
  const token = (await cookies()).get('authjs.session-token')?.value || null;
  console.log("Token in Teste page:", token);
  return (
    <div>
      <h1>Teste</h1>
    </div>
  )
}